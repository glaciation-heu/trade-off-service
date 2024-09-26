package com.glaciation.TradeOffService.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class Utils {
    private final Logger logger = LoggerFactory.getLogger(Utils.class);

    public void validateDates(Date startTime, Date endTime) {
        if (startTime.after(endTime) || startTime.equals(endTime)) {
            throw new IllegalArgumentException("Parameter 'startDate' cannot be greater than or equal to 'endDate'");
        }

        // restrictions about the valid period (months/years) ??
    }
}
