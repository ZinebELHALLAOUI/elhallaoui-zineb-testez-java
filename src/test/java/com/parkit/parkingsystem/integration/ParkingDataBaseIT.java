package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock(lenient = true)
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {

    }

    @Test
    public void testParkingACar() {
        //given
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //when
        parkingService.processIncomingVehicle();

        //then
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket); // le ticket exist dans la base
        int nextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR); // recupére la prochain numero de place disponible
        assertEquals(nextAvailableSlot, 2); // le prochain numero de place disponible doit-être different du numero 1, dans notre exemple c'est le numero de place 2 qui sera le suivant

    }

    @Test
    public void testParkingLotExit() {
        //given
        Ticket ticket = new Ticket();
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        ticket.setInTime(inTime);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticketDAO.saveTicket(ticket);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //when
        parkingService.processExitingVehicle();

        //then
        Ticket ticketAfter = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticketAfter.getOutTime());// verifie si le outime est valorisé dans la base de données
        assertTrue(ticketAfter.getPrice() != 0); // verifie si le prix est different de zero
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        //given
        Ticket ticket1 = new Ticket();
        Date inTime1 = new Date();
        inTime1.setTime(System.currentTimeMillis() - (120 * 60 * 1000));
        ticket1.setInTime(inTime1);
        ticket1.setVehicleRegNumber("ABCDEF");
        Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        ticket1.setOutTime(outTime);
        ticket1.setPrice(20);
        ticket1.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));

        ticketDAO.saveTicket(ticket1);

        Ticket ticket2 = new Ticket();
        Date inTime2 = new Date();
        inTime2.setTime(System.currentTimeMillis() - (45 * 60 * 1000));
        ticket2.setInTime(inTime2);
        ticket2.setVehicleRegNumber("ABCDEF");
        ticket2.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticketDAO.saveTicket(ticket2);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //when
        parkingService.processExitingVehicle();

        //then
        Ticket ticketAfter = ticketDAO.getTicket("ABCDEF");
        assertEquals(1.06875,ticketAfter.getPrice(), 0.01d);
    }

}
