package com.dianping.cat.system.page.abtest;

public enum Action implements org.unidal.web.mvc.Action {
	VIEW("view"),
	
	CREATE("create"),
	
	DETAIL("detail"),
	
	MODEL("model"),
	
	REPORT("report"),
	
	AJAX_CREATE("ajax_create"),
	
	AJAX_DETAIL("ajax_detail"),
	
	AJAX_ADDGROUPSTRATEGY("ajax_addGs"),
	
	AJAX_PARSEGROUPSTRATEGY("ajax_parseGs"),
	
	ABTEST_CACULATOR("caculator");

	private String m_name;

	private Action(String name) {
		m_name = name;
	}

	public static Action getByName(String name, Action defaultAction) {
		for (Action action : Action.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultAction;
	}

	@Override
	public String getName() {
		return m_name;
	}
}
