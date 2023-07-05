package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    @DisplayName("Doit vérifier que la procédure de la sortie d'une voiture se passe comme prévu")
    public void processExitingVehicleTest() throws Exception {
        //given
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(0);
        when(ticketDAO.updateTicket(ticket)).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        //when
        parkingService.processExitingVehicle();

        //then
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(new ParkingSpot(1, ParkingType.CAR, true));
    }

    @Test
    @DisplayName("Doit verifier que le procédure de l'entrée d'une voiture se passe comme prévu")
    public void testProcessIncomingVehicle() throws Exception {
        //given
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(0);

        //when
        parkingService.processIncomingVehicle();

        //then
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(new ParkingSpot(1, ParkingType.CAR , false));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
    }

    @Test
    @DisplayName("Doit verifier que la place de parking n'a pas été modifiée au le moment que le ticket égalament n'as pas été modifier ")
    public void processExitingVehicleTestUnableUpdate() throws Exception {
        //given
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.getTicket("ABCDEF")).thenReturn(ticket);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(0);
        when(ticketDAO.updateTicket(ticket)).thenReturn(false);

        //when
        parkingService.processExitingVehicle();


        //then
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
    }

    @Test
    @DisplayName("Doit vérifier la disponibilité d'une place de parking")
    public void testGetNextParkingNumberIfAvailable() {
        //given
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        //when
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        //then
        assertEquals(1, parkingSpot.getId());
        assertTrue(parkingSpot.isAvailable());
    }

    @Test
    @DisplayName("Doit verifier qu'il n y a pas de place disponibles dans le parking")
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        //given
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);


        //when
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        //then
        assertNull(parkingSpot);
    }

    @Test
    @DisplayName("Doit vérifier qu'il n y a pas de places de parking disponible si le ParkingType saisi par l'utilisateur est erroné")
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        //given
        when(inputReaderUtil.readSelection()).thenReturn(10);

        //when
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        //then
        assertNull(parkingSpot);
    }

}
