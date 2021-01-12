/*
 * Copyright 2021 Itzbenz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package Ozone.UI;

import Atom.Time.Countdown;
import Atom.Utility.Pool;
import Atom.Utility.Random;
import Ozone.Manifest;
import Ozone.Settings.BaseSettings;
import arc.Core;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import io.sentry.Sentry;
import io.sentry.SentryTransaction;
import io.sentry.SpanStatus;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Build;
import mindustry.world.Tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class WorldInformation extends BaseDialog {
	Table table = new Table();
	ScrollPane scrollPane = new ScrollPane(table);
	private Label label = new Label("World calculation began...");
	
	public WorldInformation() {
		super("World Information");
		addCloseButton();
		buttons.button("Refresh", Icon.refresh, this::setup).size(210f, 64f);
	}
	
	void setup() {
		scrollPane = new ScrollPane(table);
		cont.clear();
		table.clear();
		if (!Vars.state.isGame()) return;
		label = new Label("World calculation began...");
		label.visible = false;
		ad("Word Name", Vars.state.map.name());
		ad("Players Count", Groups.player.size());
		ad("Drawc Count", Groups.draw.size());
		ad("Build Count", Groups.build.size());
		ad("Unit Count", Groups.unit.size());
		ad("[orange]Fire[white] Count", Groups.fire.size());
		ad("Bullet Count", Groups.bullet.size());
		ad("World height", Vars.world.height());
		ad("World width", Vars.world.width());
		ad("World square", Vars.world.width() * Vars.world.height());
		table.add(label).growX();
		table.row();
		Pool.submit(() -> {
			long l = System.currentTimeMillis();
			Log.debug("World calculation began");
			SentryTransaction st = Sentry.startTransaction("world-calculation-" + Vars.world.width() + "x" + Vars.world.height());
			try {
				TreeMap<String, Integer> mainCount = new TreeMap<>();
				long totalOre = 0;
				int total = Vars.world.height() * Vars.world.width();
				int height = Vars.world.height();
				AtomicLong buildableTile = new AtomicLong();
				label.visible = true;
				label.setText("World calculation began...");
				ArrayList<Future<HashMap<String, Integer>>> futures = new ArrayList<>();
				for (int i = 0; i < height; i++) {
					int finalI = i;
					int a = i * height;
					label.setText("World indexing: " + a + "/" + total);
					futures.add(Pool.submit(() -> {//hail concurrency
						HashMap<String, Integer> count = new HashMap<>();
						//Overcautious people be like
						try {
							for (int j = 0; j < Vars.world.width(); j++) {
								try {
									Tile t = Vars.world.tileWorld(finalI, j);
									if (t == null) continue;
									if (t.staticDarkness() != 0) continue;
									try {
										count.put(t.floor().toString(), count.getOrDefault(t.floor().toString(), 0) + 1);
									}catch (Throwable ignored) {}
									try {
										count.put(t.overlay().toString(), count.getOrDefault(t.overlay().toString(), 0) + 1);
									}catch (Throwable ignored) {}
									try {
										count.put(t.block().toString(), count.getOrDefault(t.block().getDisplayName(t), 0) + 1);
									}catch (Throwable ignored) {}
									try {
										if (t.build != null)
											count.put(t.build.getDisplayName(), count.getOrDefault(t.build.getDisplayName(), 0) + 1);
									}catch (Throwable ignored) {}
									if (Build.validPlace(Blocks.copperWall, Vars.player.team(), t.x, t.y, 0))
										buildableTile.getAndIncrement();
								}catch (Throwable ignored) {}
							}
						}catch (Throwable ignored) {}
						return count;
					}));
				}
				
				long futc = futures.size();
				ArrayList<Future<?>> fut = new ArrayList<>(futures);
				for (Future<HashMap<String, Integer>> f : futures) {
					try {
						for (Map.Entry<String, Integer> s : f.get().entrySet())
							mainCount.put(s.getKey(), mainCount.getOrDefault(s.getKey(), 0) + s.getValue());
						fut.remove(f);
						Pool.submit(() -> {
							String s = "";
							try {
								s = ((((float) (futc - fut.size()) / futc) * 100)) + "%";
								s = (futc - fut.size()) + "/" + futc;
								label.setText("World calculating: " + s);
							}catch (Throwable t) {
								s = (futc - fut.size()) + "/" + futc;
								label.setText("World calculating: " + s);
							}
						});
						
					}catch (Throwable ignored) { }
				}
				for (Map.Entry<String, Integer> s : mainCount.entrySet())
					if (s.getKey().startsWith("ore-")) totalOre += s.getValue();
				
				ad("The following statistic are measured in tile (1x1)", "");
				ad("Total Ores", totalOre);
				ad("Buildable Tiles", buildableTile);
				ad(mainCount);
				Log.debug("World calculation finished in @", Countdown.result(l));
				if ((System.currentTimeMillis() - l) > 3000)
					Vars.ui.showInfo("World calculation finished: " + Countdown.result(l));
			}catch (Throwable i) {
				if (st != null) {
					st.setStatus(SpanStatus.INTERNAL_ERROR);
					st.setThrowable(i);
				}
				throw new RuntimeException(i);
			}finally {
				if (st != null) st.finish();
				label.visible = false;
			}
		});
		cont.add(scrollPane).growX().growY();
	}
	
	void ad(Map<String, ?> map) {
		for (Map.Entry<String, ?> s : map.entrySet())
			ad(s.getKey(), s.getValue());
	}
	
	void ad(String title, Callable<Object> callable) {
		Pool.submit(() -> {
			try {
				ad(title, callable.call());
			}catch (Throwable e) {
				e.printStackTrace();
				Sentry.captureException(e);
			}
		});
	}
	
	void ad(String title, Object value) {
		if (value == null) value = "null";
		if (BaseSettings.colorPatch) title = "[" + Random.getRandomHexColor() + "]" + title;
		Label l = new Label(title + ":");
		table.add(l).growX();
		String finalValue = String.valueOf(value);
		table.row();
		table.field(finalValue, s -> {
			setup();
			Core.app.setClipboardText(finalValue);
			Manifest.toast("Copied");
		}).expandX().growX();
		table.row();
	}
}