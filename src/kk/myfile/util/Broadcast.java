package kk.myfile.util;

import java.util.HashMap;
import java.util.Map;

public class Broadcast {
	public static interface IListener {
		public void onReceive(String name, Object data);
	}

	private static class Node {
		public IListener listener;
		public boolean onUiThread;

		public Node(IListener listener, boolean onUiThread) {
			this.listener = listener;
			this.onUiThread = onUiThread;
		}
	}

	private static final Map<String, Map<IListener, Node>> sMap
		= new HashMap<String, Map<IListener, Node>>();

	public static synchronized void addListener(IListener listener,
			String name, boolean onUiThread) {

		Map<IListener, Node> map = sMap.get(name);

		if (map == null) {
			map = new HashMap<IListener, Node>();
			sMap.put(name, map);
		}

		map.put(listener, new Node(listener, onUiThread));
	}

	public static synchronized void removeLsitener(IListener listener,
			String name) {
		
		if (name == null) {
			for (Map<IListener, Node> map : sMap.values()) {
				map.remove(listener);
			}
		} else {
			Map<IListener, Node> map = sMap.get(name);
	
			if (map != null) {
				map.remove(listener);
			}
		}
	}

	public static synchronized void send(final String name, final Object data) {
		Map<IListener, Node> map = sMap.get(name);

		if (map == null) {
			return;
		}

		for (Node node : map.values()) {
			final IListener listener = node.listener;

			if (node.onUiThread) {
				AppUtil.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						listener.onReceive(name, data);
					}
				});
			} else {
				AppUtil.runOnNewThread(new Runnable() {
					@Override
					public void run() {
						listener.onReceive(name, data);
					}
				});
			}
		}
	}
}
