package com.alogic.rpc.spring;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alogic.rpc.Call;
import com.alogic.rpc.InvokeContext;
import com.alogic.rpc.InvokeFilter;
import com.alogic.rpc.Parameters;
import com.alogic.rpc.Result;
import com.alogic.rpc.Parameters.Default;
import com.alogic.rpc.message.RPCMessage;
import com.alogic.rpc.serializer.Serializer;
import com.alogic.rpc.serializer.kryo.KryoSerializer;
import com.anysoft.util.Factory;
import com.anysoft.util.IOTools;
import com.anysoft.util.JsonTools;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.Settings;
import com.logicbus.backend.Context;
import com.logicbus.backend.Servant;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.message.JsonMessage;
import com.logicbus.backend.server.http.HttpContext;
import com.logicbus.models.servant.ServiceDescription;

public class SpringServant extends Servant {
	protected Call theCall = null;
	protected Serializer serializer = null;
	protected InvokeFilter filter = null;
	private String serviceId;

	@Override
	public int actionProcess(Context ctx) {
		String method = getArgument("method", "$list", ctx);
		if (method.equals("$list")) {
			list(ctx);
			return 0;
		}

		RPCMessage msg = (RPCMessage) ctx.asMessage(RPCMessage.class);
		InputStream in = msg.getInputStream();
		Parameters params = (Parameters) serializer.readObject(in, Parameters.Default.class,ctx);
		if (params == null){
			params = new Parameters.Default();
		}
		if (filter != null) {
			InvokeContext invokeContext = params.context();
			if (invokeContext != null) {
				filter.doFilter(invokeContext);
			}
		}

		Result result = theCall.invoke(serviceId, method, params);
		result.host(ctx.getHost());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			serializer.writeObject(bos, result,ctx);
			bos.flush();
			byte[] bytes = bos.toByteArray();
			((HttpContext) ctx).getResponse().setHeader("Content-Length", String.valueOf(bytes.length));
			OutputStream out = msg.getOutputStream();
			out.write(bytes);
		} catch (IOException e) {
			logger.error("Action process error!", e);
		} finally {
			IOTools.close(bos);
		}
		return 0;
	}

	private void list(Context ctx) {
		JsonMessage msg = (JsonMessage) ctx.asMessage(JsonMessage.class);

		String beanId = null;
		Class<?> _interface = null;
		if (StringUtils.contains(serviceId, ".")) {
			try {
				_interface = Settings.getClassLoader().loadClass(serviceId);
			} catch (ClassNotFoundException e) {
				throw new ServantException("core.instance_failed", "Class not found: " + serviceId);
			}
		} else {
			beanId = serviceId;
		}

		Object instance = SpringObjectFactory.getBean(beanId, _interface);
		
		if (instance == null) {
			throw new ServantException("core.impl_is_null", "The impl instance is null.");
		}

		Map<String, Object> root = msg.getRoot();

		Map<String, Object> service = new HashMap<String, Object>();

		Class<?> clazz = instance.getClass();

		List<Object> methodList = new ArrayList<Object>();

		Method[] methods = clazz.getDeclaredMethods();
		for (Method m : methods) {
			if (Modifier.isPublic(m.getModifiers())) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("name", m.getName());
				map.put("return", m.getReturnType().getName());

				List<Object> parameterList = new ArrayList<Object>();

				Class<?>[] paramClasses = m.getParameterTypes();
				for (Class<?> c : paramClasses) {
					parameterList.add(c.getName());
				}

				map.put("parameters", parameterList);

				methodList.add(map);
			}
		}

		service.put("methods", methodList);
		JsonTools.setString(service, "module", clazz.getName());
		root.put("service", service);
	}

	public void create(ServiceDescription sd) {
		super.create(sd);

		Properties p = sd.getProperties();

		serviceId = sd.getServiceID();
		theCall = new SpringCall();

		String serializerClass = PropertiesConstants.getString(p, "rpc.serializer", KryoSerializer.class.getName());
		Factory<Serializer> factory = new Factory<Serializer>();
		try {
			serializer = factory.newInstance(serializerClass, p);
		} catch (Exception ex) {
			serializer = new KryoSerializer();
			serializer.configure(p);
			logger.error("Can not create serializer,use default:" + serializer.getClass().getName(), ex);
		}

		String filterClass = PropertiesConstants.getString(p, "rpc.filter", "");
		if (StringUtils.isNotEmpty(filterClass)) {
			try {
				Factory<InvokeFilter> f = new Factory<InvokeFilter>();
				filter = f.newInstance(filterClass, p);
			} catch (Exception ex) {
				logger.error("Can not create filter,module=" + filterClass);
			}
		}
	}

	public void close() {
		super.close();
	}
}
