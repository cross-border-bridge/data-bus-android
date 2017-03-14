// Copyright © 2017 DWANGO Co., Ltd.
package jp.co.dwango.cbb.db;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

public class MultiplexDataBusTest {
	private MemoryQueueDataBus sender;
	private MemoryQueueDataBus receiver;
	private int counter;

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
	public void 送信データ形式の確認() {
		before();
		DataBus dataBus = new MultiplexDataBus(sender, "TestSlot");
		receiver.addHandler(new DataBusHandler() {
			@Override
			public void onReceive(JSONArray data) {
				try {
					Assert.assertEquals(4, data.length());
					Assert.assertEquals("TestSlot", data.getString(0));
					Assert.assertEquals("one", data.getString(1));
					Assert.assertEquals(2, data.getInt(2));
					Assert.assertTrue(data.getBoolean(3));
				} catch (JSONException e) {
					Assert.fail(e.toString());
				}
			}
		});
		dataBus.send(new JSONArray().put("one").put(2).put(true));
		dataBus.destroy();
		after();
	}

	@Test
	public void 異なるdataIdのデータを受信しないことの確認() {
		before();
		DataBus dataBus = new MultiplexDataBus(receiver, "TestSlot");
		dataBus.addHandler(new DataBusHandler() {
			@Override
			public void onReceive(JSONArray data) {
				Assert.fail();
			}
		});
		sender.send(new JSONArray().put("TestSlotX").put("one").put(2).put(true));
		dataBus.destroy();
		after();
	}

	@Test
	public void 一致するdataIdのデータを受信してdataIdが無くなることの確認() {
		before();
		DataBus dataBus = new MultiplexDataBus(receiver, "TestSlot");
		dataBus.addHandler(new DataBusHandler() {
			@Override
			public void onReceive(JSONArray data) {
				Assert.assertEquals(3, data.length());
				try {
					Assert.assertEquals("one", data.getString(0));
					Assert.assertEquals(2, data.getInt(1));
					Assert.assertTrue(data.getBoolean(2));
				} catch (JSONException e) {
					Assert.fail(e.toString());
				}
			}
		});
		sender.send(new JSONArray().put("TestSlot").put("one").put(2).put(true));
		dataBus.destroy();
		after();
	}

	@Test
	public void nullを含むJSONを送受信() {
		before();
		counter = 0;
		DataBus dataBusR = new MultiplexDataBus(receiver, "TestSlot");
		dataBusR.addHandler(new DataBusHandler() {
			@Override
			public void onReceive(JSONArray data) {
				Assert.assertEquals(3, data.length());
				try {
					Assert.assertEquals("one", data.getString(0));
					Assert.assertTrue(data.isNull(1));
					Assert.assertTrue(data.getBoolean(2));
				} catch (JSONException e) {
					Assert.fail(e.toString());
				}
				counter++;
			}
		});
		DataBus dataBusS = new MultiplexDataBus(sender, "TestSlot");
		dataBusS.send(new JSONArray().put("one").put(null).put(true));
		Assert.assertEquals(1, counter);
		dataBusR.destroy();
		dataBusS.destroy();
		after();
	}

	@Test
	public void 多階層のテスト() {
		before();
		DataBus dataBusS1 = new MultiplexDataBus(sender, "layer1");
		DataBus dataBusS2 = new MultiplexDataBus(dataBusS1, "layer2");
		DataBus dataBusS3 = new MultiplexDataBus(dataBusS2, "layer3");
		DataBus dataBusR1 = new MultiplexDataBus(receiver, "layer1");
		DataBus dataBusR2 = new MultiplexDataBus(dataBusR1, "layer2");
		DataBus dataBusR3 = new MultiplexDataBus(dataBusR2, "layer3");

		// 最下層では全layerが先頭に挿入された状態のデータを受信する
		receiver.addHandler(new DataBusHandler() {
			@Override
			public void onReceive(JSONArray data) {
				Assert.assertEquals(4, data.length());
				try {
					Assert.assertEquals("layer1", data.getString(0));
					Assert.assertEquals("layer2", data.getString(1));
					Assert.assertEquals("layer3", data.getString(2));
					Assert.assertEquals("data", data.getString(3));
				} catch (JSONException e) {
					Assert.fail(e.toString());
				}
			}
		});

		// 第1層では第1層のdataIdが削除されたデータを受信する
		dataBusR1.addHandler(new DataBusHandler() {
			@Override
			public void onReceive(JSONArray data) {
				Assert.assertEquals(3, data.length());
				try {
					Assert.assertEquals("layer2", data.getString(0));
					Assert.assertEquals("layer3", data.getString(1));
					Assert.assertEquals("data", data.getString(2));
				} catch (JSONException e) {
					Assert.fail(e.toString());
				}
			}
		});

		// 第2層では第1〜2層のdataIdが削除されたデータを受信する
		dataBusR2.addHandler(new DataBusHandler() {
			@Override
			public void onReceive(JSONArray data) {
				Assert.assertEquals(2, data.length());
				try {
					Assert.assertEquals("layer3", data.getString(0));
					Assert.assertEquals("data", data.getString(1));
				} catch (JSONException e) {
					Assert.fail(e.toString());
				}
			}
		});

		// 最上位層では全てのdataIdが削除されたデータを受信する
		dataBusR3.addHandler(new DataBusHandler() {
			@Override
			public void onReceive(JSONArray data) {
				Assert.assertEquals(1, data.length());
				try {
					Assert.assertEquals("data", data.getString(0));
				} catch (JSONException e) {
					Assert.fail(e.toString());
				}
			}
		});

		// 最上位層でデータを送る
		dataBusS3.send(new JSONArray().put("data"));
		after();
	}
}
