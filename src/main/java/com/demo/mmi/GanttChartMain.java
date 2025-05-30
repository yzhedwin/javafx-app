package com.demo.mmi;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.demo.mmi.chart.GanttChart;
import com.demo.openapi.service.RestAPIGateway;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

@SpringBootApplication(scanBasePackages = { "com.demo.openapi" })
public class GanttChartMain extends Application {

	private ConfigurableApplicationContext context;

	@Autowired
	private RestAPIGateway restAPIGateway;

	@Override
	public void start(Stage var1) throws Exception {
		GanttChart gc = new GanttChart(800, 400);
		Scene s = new Scene(gc);
		var1.setScene(s);
		var1.show();

		gc.getModel().getModelEventProperty().addListener((obj, oldVal, newVal) -> {
			if (newVal != null) {
				switch (newVal.getModelEvent()) {
					// case ADD -> System.out.println("Task added: " + newVal.toString());
					// case REMOVE -> System.out.println("Task removed: " + newVal.toString());
					case CHANGE -> {
						restAPIGateway.send(newVal);
					}
					default -> System.out.println("Unknown event type: " + newVal.getModelEvent());
				}
			}
		});

		gc.getModel().addTaskGroup("zzom");
		gc.getModel().addTaskGroup("zzom duo");
		gc.getModel().addTaskGroup("zzom zz");

		gc.getModel().addTask("zzom", "Hehe", Color.RED, ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(25));

		gc.getModel().addTask("zzom duo", "Haha", Color.BLUE, ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(30));

		gc.getModel().addTask("zzom zz", "Hoho", Color.BLUE, ZonedDateTime.now(), ZonedDateTime.now().plusSeconds(30));

	}

	@Override
	public void init() throws Exception {
		context = new SpringApplicationBuilder(GanttChartMain.class).run();
		restAPIGateway = context.getBean(RestAPIGateway.class);
	}

	@Override
	public void stop() throws Exception {
		context.close();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
