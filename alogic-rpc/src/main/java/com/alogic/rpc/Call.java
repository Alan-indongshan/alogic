package com.alogic.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.anysoft.util.Configurable;
import com.anysoft.util.Factory;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.Reportable;
import com.anysoft.util.XMLConfigurable;
import com.anysoft.util.XmlElementProperties;
import com.anysoft.util.XmlTools;
import com.esotericsoftware.minlog.Log;


/**
 * 过程调用
 * 
 * @author duanyy
 *
 * @since 1.6.7.15
 */
public interface Call extends Reportable,Configurable,XMLConfigurable{

	/**
	 * 同步调用过程
	 * 
	 * @param id id
	 * @param method 调用方法 
	 * @param params 参数列表
	 * @return 调用结果
	 */
	public Result invoke(String id,String method,Parameters params);
	
	/**
	 * 异步过程调用
	 * @param id id
	 * @param method 调用方法
	 * @param params 参数列表
	 * @return Future实例
	 */
	public CallFuture invokeAsync(String id,String method,Parameters params);
	
	/**
	 * 异步过程调用
	 * @param id id
	 * @param method 调用方法
	 * @param params 参数列表
	 * @param callback 回调方法
	 */
	public void invokeAsync(String id,String method,Parameters params,Callback callback);
	
	/**
	 * 异步过程调用
	 * @param id id
	 * @param method 调用方法
	 * @param params 参数列表
	 * @param callback 回调方法
	 * @param ctx 调用上下文
	 */
	public void invokeAsync(String id,String method,Parameters params,Callback callback,Object ctx);
	
	/**
	 * 创建新的参数
	 * @return 参数对象实例
	 */
	public Parameters newParameters();
	
	/**
	 * 虚基类实现
	 * 
	 * @author duanyy
	 *
	 */
	public abstract static class Abstract implements Call{
		/**
		 * a logger of slf4j
		 */
		protected static final Logger LOG = LoggerFactory.getLogger(Call.class);
		
		protected ScheduledThreadPoolExecutor exec = null;
		
		protected List<InvokeFilter> filters = new ArrayList<InvokeFilter>();
		
		@Override
		public void report(Element xml) {
			if (xml != null){
				xml.setAttribute("module", getClass().getName());
			}
		}

		@Override
		public void report(Map<String, Object> json) {
			if (json != null){
				json.put("module", getClass().getName());
			}
		}
		
		@Override
		public void configure(Element e, Properties p) {
			XmlElementProperties props = new XmlElementProperties(e,p);
			configure(props);
			
			exec = new ScheduledThreadPoolExecutor(PropertiesConstants.getInt(p, "rpc.async.poolsize", 100));
			
			NodeList nodeList = XmlTools.getNodeListByPath(e, "filter");
			
			Factory<InvokeFilter> factory = new Factory<InvokeFilter>();
			for (int i = 0 ;i < nodeList.getLength() ; i ++){
				Node n = nodeList.item(i);
				if (Node.ELEMENT_NODE != n.getNodeType()){
					continue;
				}
				
				Element elem = (Element)n;
				
				try {
					InvokeFilter filter = factory.newInstance(elem, props, "module");
					if (filter != null){
						filters.add(filter);
					}
				}catch (Exception ex){
					Log.error("Can not create filter instance with xml:" + XmlTools.node2String(elem), ex);
				}
			}
		}
		
		@Override
		public Parameters newParameters(){
			return new Parameters.Default();
		}
		
		@Override
		public CallFuture invokeAsync(final String id,final String method,final Parameters params){
			ScheduledFuture<Result> f = exec.schedule(new Callable<Result>(){
					@Override
					public Result call() {
						return invoke(id,method,params);
					}
				}, 0, TimeUnit.MICROSECONDS);
			
			return new CallFutureImpl(f);
		}	
		
		@Override
		public void invokeAsync(final String id,final String method,final Parameters params,final Callback callback,final Object ctx){
			exec.schedule(new Runnable(){
				@Override
				public void run() {
					Result result = null;
					try {
						result = invoke(id,method,params);
						if (callback != null){
							callback.onFinish(id,method,result,params,ctx);
						}
					}catch (Exception ex){
						if (callback != null){
							callback.onException(id, method, result,params, ctx, ex);
						}
					}
				}
			}, 0, TimeUnit.MICROSECONDS);
		}
		
		@Override
		public void invokeAsync(String id,String method,Parameters params,Callback callback){
			invokeAsync(id,method,params,callback,null);
		}
	}
	
	/**
	 * Future实现
	 * @author duanyy
	 *
	 */
	public static class CallFutureImpl implements CallFuture{
		protected ScheduledFuture<Result> future = null;
		
		public CallFutureImpl(ScheduledFuture<Result> f){
			future = f;
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return future.cancel(mayInterruptIfRunning);
		}

		@Override
		public boolean isCancelled() {
			return future.isCancelled();
		}

		@Override
		public boolean isDone() {
			return future.isDone();
		}

		@Override
		public Result get() throws InterruptedException, ExecutionException {
			return future.get();
		}

		@Override
		public Result get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			return future.get(timeout, unit);
		}
	}
}
