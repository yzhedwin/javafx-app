package com.demo.mmi;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import com.demo.common.model.SimpleAssetMission;
import com.demo.mmi.chart.GanttChart;
import com.demo.openapi.config.IntegrationConfig;
import com.demo.openapi.gateway.GatewayInterface;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("!test")
@SpringBootApplication(scanBasePackages = { "com.demo.openapi", "com.demo.mmi" })
@Import(IntegrationConfig.class)
public class GanttChartMain extends Application {

	private ConfigurableApplicationContext context;
	private final GanttChart gc = new GanttChart(800, 400);

	@Autowired
	private GatewayInterface gateway;

	@Autowired
	Environment env;

	@Override
	public void start(Stage var1) throws Exception {
		Scene s = new Scene(gc);
		var1.setScene(s);
		var1.show();

		gc.getModel().getModelEventProperty().addListener((obj, oldVal, newVal) -> {
			if (newVal != null) {
				switch (newVal.getEventSource()) {
					case INTERNAL -> {
						// For testing
						if (Arrays.toString(env.getActiveProfiles()).contains("server")) {
							gateway.send(newVal.getNewTask());
						} else {
							newVal.processInternalModelEvent(gateway);
						}
					}
					case EXTERNAL -> log.debug("External event: " + newVal.getModelEvent());
					default -> log.debug("Unknown event type: " + newVal.getModelEvent());
				}
			}
		});
		gc.getModel().addTaskGroup("zzom");
		gc.getModel().addTaskGroup("zzom duo");
		gc.getModel().addTaskGroup("zzom zz");

		// fetch list of mission task from endpoint
		List<SimpleAssetMission> missionList = gateway.receive();

		for (SimpleAssetMission mission : missionList) {
			gc.getModel().addTask("zzom", mission.getTitle(), Color.color(Math.random(),
					Math.random(), Math.random()),
					Instant.ofEpochMilli(mission.getAbsoluteStartTimeEpochMs()).atZone(ZoneId.systemDefault()),
					Instant.ofEpochMilli(mission.getAbsoluteStartTimeEpochMs() +
							mission.getDurationMs())
							.atZone(ZoneId.systemDefault()));
		}

		// gc.getModel().addTask("zzom duo", "Haha", Color.BLUE, ZonedDateTime.now(),
		// ZonedDateTime.now().plusSeconds(30));

		// gc.getModel().addTask("zzom zz", "Hoho", Color.BLUE, ZonedDateTime.now(),
		// ZonedDateTime.now().plusSeconds(30));

	}

	@Override
	public void init() throws Exception {
		context = new SpringApplicationBuilder(GanttChartMain.class)
				.initializers(ctx -> ctx.getBeanFactory().registerSingleton("ganttChartModel", gc.getModel()))
				.run();
		gateway = context.getBean(GatewayInterface.class);
		env = context.getBean(Environment.class);
	}

	@Override
	public void stop() throws Exception {
		context.close();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
