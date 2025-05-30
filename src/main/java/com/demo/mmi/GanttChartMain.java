package com.demo.mmi;

import java.time.ZonedDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.demo.mmi.chart.GanttChart;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

@SpringBootApplication
public class GanttChartMain extends Application {

	private ConfigurableApplicationContext context;

	@Override
	public void start(Stage var1) throws Exception {
		GanttChart gc = new GanttChart(800, 400);
		Scene s = new Scene(gc);
		var1.setScene(s);
		var1.show();

		gc.getModel().getModelEventProperty().addListener((obj, oldVal, newVal) -> {
			System.out.println(newVal);
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
		context = SpringApplication.run(GanttChartMain.class);
	}

	@Override
	public void stop() throws Exception {
		context.close();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
