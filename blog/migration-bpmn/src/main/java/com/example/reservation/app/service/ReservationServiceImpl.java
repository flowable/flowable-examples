package com.example.reservation.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Simon Amport
 */
@Service("reservationService")
public class ReservationServiceImpl implements ReservationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationServiceImpl.class);

    @Override
    public void reserveTable() {
        LOGGER.info("Table reserved.");
    }

    @Override
    public void releaseTable() {
        LOGGER.info("Table released.");
    }

    @Override
    public boolean showUp() {
        return true;
    }

}
