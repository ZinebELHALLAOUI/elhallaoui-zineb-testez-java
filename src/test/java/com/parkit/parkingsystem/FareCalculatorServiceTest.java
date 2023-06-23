package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    @DisplayName("Doit payer 1.5 pour une voiture qui est garée pendant une heure")
    public void calculateFareCar() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), 1.5);
    }

    @Test
    @DisplayName("Doit payer 1.0 pour un vélo qui est garé pendant une heure")
    public void calculateFareBike() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), 1.0);
    }

    @Test
    @DisplayName("Doit renvoyer une exception de type IllegalArgumentException lorsque le vehicule n'est ni une voiture, ni un velo")
    public void calculateFareUnkownType() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.UNKNOWN, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
        assertEquals("Unknown Parking Type", thrown.getMessage());

    }

    @Test
    @DisplayName("")
    public void calculateFareBikeWithFutureInTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
        assertEquals("Out time provided is incorrect:" + ticket.getOutTime().toString(), thrown.getMessage());
    }

    @Test
    @DisplayName("Le prix du ticket d'un velo doit être 0.75 quand il se gare pendant 45 minutes")
    public void calculateFareBikeWithLessThanOneHourParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(0.75, ticket.getPrice());
    }

    @Test
    @DisplayName("Le prix du ticket d'une voiture doit être 1.125 quand elle se gare pendant 45 minutes")
    public void calculateFareCarWithLessThanOneHourParkingTime() {
        //given
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        //when

        fareCalculatorService.calculateFare(ticket);

        //then
        assertEquals(1.125, ticket.getPrice());
    }

    @Test
    @DisplayName("Le prix du ticket d'une voiture doit être 36 quand elle se gare pendant 24 heures")
    public void calculateFareCarWithMoreThanADayParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(36, ticket.getPrice());
    }

    @Test
    @DisplayName("Le prix du ticket doit être 0 pour une voiture qui s'est garée pendant moins de 30 minutes")
    public void calculateFareCarWithLessThan30minutesParkingTime() {
        //given
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (15 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);


        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        //when
        fareCalculatorService.calculateFare(ticket);

        //then
        assertEquals(0, ticket.getPrice());
    }

    @Test
    @DisplayName("Le prix du ticket doit être 0 pour un vélo qui s'est garé pendant moins de 30 minutes")
    public void calculateFareBikeWithLessThan30minutesParkingTime() {
        //given
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (15 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        //when
        fareCalculatorService.calculateFare(ticket);

        //then
        assertEquals(0, ticket.getPrice());
    }

    @Test
    @DisplayName("Doit renvoyer une exception de type IllegalArgumentException quand le ticket est null")
    public void calculateNullTicket() {
        //given
        Ticket ticket = null;

        //when then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
        assertEquals("Ticket can not be null value", thrown.getMessage());
    }

    @Test
    @DisplayName("Losqu'une voiture stationne Pour 45 minutes, le ticket  avec reduction doit couter 1.06875 au lieu de 1.125 soit 5% de reduction")
    public void calculateFareCarWithDiscount() {
        //given
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        //when
        fareCalculatorService.calculateFare(ticket, true);

        //then
        assertEquals(1.06875, ticket.getPrice(), 0.000001d);
    }

    @Test
    @DisplayName("Losqu'un vélo stationne Pour 45 minutes, le ticket  avec reduction doit couter 1.06875 au lieu de 1.125 soit 5% de reduction")
    public void calculateFareBikeWithDiscount() {
        //given
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        //when
        fareCalculatorService.calculateFare(ticket, true);

        //then
        assertEquals(ticket.getPrice(), 0.7125, 0.000001d);

    }
}
