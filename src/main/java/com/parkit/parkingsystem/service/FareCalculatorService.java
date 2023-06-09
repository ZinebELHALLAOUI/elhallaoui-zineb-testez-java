package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public static final double FIVE_PERCENT_DISCOUNT = 0.95d;

    public void calculateFare(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket can not be null value");
        }

        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        long duration = outHour - inHour;

        if (duration < (30 * 60 * 1000)) {
            ticket.setPrice(0);
            return;
        }

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice((duration * Fare.CAR_RATE_PER_HOUR) / 3_600_000);
                break;
            }
            case BIKE: {
                ticket.setPrice((duration * Fare.BIKE_RATE_PER_HOUR) / 3_600_000);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }
    }

    public void calculateFare(Ticket ticket, boolean discount) {
        this.calculateFare(ticket);
        if (discount) {
            double standardPrice = ticket.getPrice();
            ticket.setPrice(standardPrice * FIVE_PERCENT_DISCOUNT);// discount of 5%
        }
    }
}