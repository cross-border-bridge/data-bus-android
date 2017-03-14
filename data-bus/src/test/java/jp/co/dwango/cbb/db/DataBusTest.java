// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.db;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DataBusTest {
	private MemoryQueueDataBus sender;
	private MemoryQueueDataBus receiver;

	@Before
	public void setUp() {
		DataBus.logging(false);
	}

	private void before() {
		MemoryQueue queue1 = new MemoryQueue();
		MemoryQueue queue2 = new MemoryQueue();
		sender = new MemoryQueueDataBus(queue1, queue2);
		receiver = new MemoryQueueDataBus(queue2, queue1);
	}

	private void after() {
		sender.destroy();
		receiver.destroy();
	}

	@Test
	public void 送信したデータが期待する形で受信できることの検証() {
		before();
		receiver.addHandler(new DataBusHandler() {
			@Override
			public void onReceive(JSONArray message) {
				Assert.assertEquals("[\"HELLO\",1,2,3]", message.toString());
			}
		});
		sender.send(new JSONArray().put("HELLO").put(1).put(2).put(3));
		after();
	}

	@Test
	public void 送信したデータが複数ハンドラで期待する形で受信できることの検証() {
		before();
		final AtomicReference<List<String>> successCounter = new AtomicReference<List<String>>(new ArrayList<String>());
		receiver.addHandler(new DataBusHandler() {
			@Override
			public void onReceive(JSONArray message) {
				Assert.assertEquals("[\"HELLO\",1,2,3]", message.toString());
				successCounter.get().add(message.toString());
			}
		});
		receiver.addHandler(new DataBusHandler() {
			@Override
			public void onReceive(JSONArray message) {
				Assert.assertEquals("[\"HELLO\",1,2,3]", message.toString());
				successCounter.get().add(message.toString());
			}
		});
		receiver.addHandler(new DataBusHandler() {
			@Override
			public void onReceive(JSONArray message) {
				Assert.assertEquals("[\"HELLO\",1,2,3]", message.toString());
				successCounter.get().add(message.toString());
			}
		});
		sender.send(new JSONArray().put("HELLO").put(1).put(2).put(3));
		Assert.assertEquals(3, successCounter.get().size());
		after();
	}

	@Test
	public void 登録した複数ハンドラの一部をremoveした時removeしたハンドラのonReceiveのみ発火しないことの検証() {
		before();
		final AtomicReference<List<String>> successCounter = new AtomicReference<List<String>>(new ArrayList<String>());
		DataBusHandler handlerA = new DataBusHandler() {
			@Override
			public void onReceive(JSONArray message) {
				Assert.fail();
			}
		};
		DataBusHandler handlerN = new DataBusHandler() {
			@Override
			public void onReceive(JSONArray message) {
				successCounter.get().add("success");
			}
		};
		receiver.addHandler(handlerA);
		receiver.addHandler(handlerN);
		receiver.removeHandler(handlerA); // remove abnormal handler
		sender.send(new JSONArray().put("HELLO").put(1).put(2).put(3));
		Assert.assertEquals(1, successCounter.get().size());
		after();
	}

	@Test
	public void 同一のハンドラが重複登録されないことの検証() {
		before();
		DataBusHandler h = new DataBusHandler() {
			@Override
			public void onReceive(JSONArray message) {
				Assert.fail();
			}
		};
		receiver.addHandler(h);
		receiver.addHandler(h);
		Assert.assertEquals(1, receiver.getHandlerCount());
		receiver.destroy();
		Assert.assertEquals(0, receiver.getHandlerCount());
		after();
	}

	@Test
	public void destroy後は何もできないことの検証() {
		before();
		sender.destroy();
		receiver.destroy();
		DataBusHandler h = new DataBusHandler() {
			@Override
			public void onReceive(JSONArray message) {
				Assert.fail();
			}
		};
		receiver.addHandler(h);
		Assert.assertEquals(0, receiver.getHandlerCount());
		sender.send(new JSONArray().put("HELLO").put(1).put(2).put(3));
		receiver.removeHandler(h);
		receiver.removeAllHandlers();
		sender.destroy();
		receiver.destroy();
		after();
	}

	@Test
	public void dataBusName指定での全ハンドラ解除() {
		before();
		DataBusHandler h1 = new DataBusHandler() {
			@Override
			public void onReceive(JSONArray message) {
				Assert.fail();
			}
		};
		DataBusHandler h2 = new DataBusHandler() {
			@Override
			public void onReceive(JSONArray message) {
				Assert.fail();
			}
		};
		receiver.addHandler(h1);
		receiver.addHandler(h2);
		Assert.assertEquals(2, receiver.getHandlerCount());
		receiver.removeAllHandlers();
		Assert.assertEquals(0, receiver.getHandlerCount());
		after();
	}

	@Test
	public void handler登録前にsendした内容がロストすることの検証() {
		before();
		sender.send(new JSONArray().put("Hello"));
		receiver.addHandler(new DataBusHandler() {
			@Override
			public void onReceive(JSONArray message) {
				Assert.fail();
			}
		});
		after();
	}
}