package com.alogic.cube.xscript;

import com.alogic.cube.mdr.DataCell;
import com.alogic.cube.mdr.DataRow;
import com.alogic.xscript.ExecuteWatcher;
import com.alogic.xscript.Logiclet;
import com.alogic.xscript.LogicletContext;
import com.alogic.xscript.doc.XsObject;
import com.alogic.xscript.plugins.Segment;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;

/**
 * 合计
 * @author yyduan
 * @since 1.6.11.35
 */
public class XsTotalCell extends Segment{
	protected String pid = "$cube-row";
	protected String cid = "$cube-cell";
	
	public XsTotalCell(String tag, Logiclet p) {
		super(tag, p);
	}

	@Override
	public void configure(Properties p){
		super.configure(p);
		
		pid = PropertiesConstants.getString(p,"pid",pid,true);
		cid = PropertiesConstants.getString(p,"cid",cid,true);
	}
	
	@Override
	protected void onExecute(final XsObject root,final XsObject current, final LogicletContext ctx, final ExecuteWatcher watcher) {
		DataRow row = ctx.getObject(pid);
		if (row != null){
			try {
				DataCell cell = row.getTotal();
				if (cell != null){
					ctx.setObject(cid, cell);
				}
				super.onExecute(root, current, ctx, watcher);
			}finally{
				ctx.removeObject(cid);
			}
		}
	}
}
