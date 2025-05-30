package com.demo.mmi.util;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import javafx.geometry.Insets;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public final class GanttChartUtil {
	public enum EModelEvent {
		ADD, CHANGE, REMOVE
	}

	public enum EColourUtil {
		AMBER_50("#ff8424"), GREY_100("#121212");

		private String hexColour;
		private Color colour;

		private EColourUtil(String hex) {
			hexColour = hex;
			colour = Color.web(hex);
		}

		public String getHex() {
			return hexColour;
		}

		public Color getColour() {
			return colour;
		}
	}

	public static final String CSS_PATH = "/common/css/ganttchart/GanttChart.css";
	public static final String CSS_MENU_PATH = "/common/css/ganttchart/GanttChartMenu.css";
	public static final String CSS_GANTTCHART = "gantt-chart";
	public static final String CSS_TASK_GROUP_LIST_INFORMATION = "task-group-list-information";
	public static final String CSS_TASK_GROUP_LIST_DATA = "task-group-list-data";
	public static final String CSS_TIMELINE = "timeline";
	public static final String CSS_TIMELINE_LABEL = "timeline-label";
	public static final String CSS_GUIDER = "guider";
	public static final String CSS_GUIDER_LINE = "guider-line";
	public static final String CSS_GUIDER_BUTTON = "guider-button";
	public static final String CSS_GUIDER_TRACK = "guider-track";
	public static final String CSS_TASK_LIST = "task-list";
	public static final String CSS_ROW_INDICATOR = "row-indicator";
	public static final String CSS_CONTEXT_MENU = "gantt-chart-context-menu";

	public static final int COLUMN_COUNT = 6;
	public static final double TASK_HEIGHT = 50; // in pixels. TODO change to ratio
	public static final double GUIDER_HEIGHT = 20; // in pixels. TODO change to ratio
	public static final double GUIDER_WIDTH = GUIDER_HEIGHT * 1.61805;
	public static final Color GUIDER_COLOUR = EColourUtil.AMBER_50.getColour();
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEEE\ndd-MM-YY\nHH:mm:ss");
	public static final Insets TIMELINE_PADDING = new Insets(5, 5, 5, 5);
	public static final Border TIMELINE_UNIT_BORDER = new Border(
			new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 1, 0, 1)));

	public static final String MENU_TASK_LIST = "task-list";
	public static final String MENU_SCHEDULED_TASK = "scheduled-task";
	public static final String MENU_ADD = "Add";
	public static final String MENU_DELETE = "Delete";

	public static final int INITIAL_STEP_INDEX = 1;
	public static final DateTimeStep[] DATE_TIME_STEPS = new DateTimeStep[] { new DateTimeStep(10, TimeUnit.SECONDS),
			new DateTimeStep(30, TimeUnit.SECONDS), new DateTimeStep(1, TimeUnit.MINUTES),
			new DateTimeStep(2, TimeUnit.MINUTES), new DateTimeStep(5, TimeUnit.MINUTES),
			new DateTimeStep(15, TimeUnit.MINUTES), new DateTimeStep(30, TimeUnit.MINUTES),
			new DateTimeStep(1, TimeUnit.HOURS), new DateTimeStep(2, TimeUnit.HOURS),
			new DateTimeStep(3, TimeUnit.HOURS), new DateTimeStep(6, TimeUnit.HOURS),
			new DateTimeStep(12, TimeUnit.HOURS), new DateTimeStep(1, TimeUnit.DAYS),
			new DateTimeStep(2, TimeUnit.DAYS), new DateTimeStep(7, TimeUnit.DAYS),
			new DateTimeStep(14, TimeUnit.DAYS) };

	public static final String DEFAULT_TASK_NAME = "Task ";
	public static final Color DEFAULT_TASK_COLOUR = EColourUtil.GREY_100.getColour();

	public enum EResizeDirection {
		NIL, EAST, WEST
	}

	private static long INTERNAL_ID = 0;

	private GanttChartUtil() {
	}

	public static long getNewTaskId() {
		return INTERNAL_ID++;
	}
}
