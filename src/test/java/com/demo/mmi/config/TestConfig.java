package com.demo.mmi.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.demo.mmi.entity.GanttChartModel;

@Configuration
public class TestConfig {

    @Bean
    @ConditionalOnMissingBean
    public GanttChartModel ganttChartModel() {
        return new GanttChartModel();
    }

}
