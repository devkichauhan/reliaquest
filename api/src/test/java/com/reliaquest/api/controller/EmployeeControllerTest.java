package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.entity.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private List<Employee> employees =
            Arrays.asList(new Employee("1", "Devki", 100, 30, "Engineer", "dev123@test.com"));

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllEmployees_Success() {
        when(employeeService.fetchAllEmployees()).thenReturn(employees);

        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetAllEmployees_RateLimit() {
        when(employeeService.fetchAllEmployees())
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests"));

        HttpClientErrorException exception =
                assertThrows(HttpClientErrorException.class, () -> employeeController.getAllEmployees());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
    }

    @Test
    void testGetAllEmployees_Empty() {
        when(employeeService.fetchAllEmployees()).thenReturn(Collections.emptyList());

        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testGetEmployeesByNameSearch_Success() {
        when(employeeService.findEmployeesByNameMatchesOrContains("Devki")).thenReturn(employees);

        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch("Devki");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetEmployeesByNameSearch_NoMatch() {
        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch("Chauhan");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testGetEmployeeById_Success() {
        when(employeeService.fetchEmployeeById("1")).thenReturn(employees.get(0));

        ResponseEntity<Employee> response = employeeController.getEmployeeById("1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Devki", response.getBody().getEmployee_name());
    }

    @Test
    void testGetEmployeeById_NotFound() {
        when(employeeService.fetchEmployeeById("2")).thenThrow(new RuntimeException("Employee not found"));

        Exception exception = assertThrows(RuntimeException.class, () -> employeeController.getEmployeeById("2"));
        assertEquals("Employee not found", exception.getMessage());
    }

    @Test
    void testGetHighestSalaryOfEmployees_Success() {
        when(employeeService.fetchHighestSalaryAmongAllEmployees()).thenReturn(100000);

        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(100000, response.getBody());
    }

    @Test
    void testGetHighestSalaryOfEmployees_InternalServerError() {
        when(employeeService.fetchHighestSalaryAmongAllEmployees())
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"));

        HttpServerErrorException exception =
                assertThrows(HttpServerErrorException.class, () -> employeeController.getHighestSalaryOfEmployees());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    }

    @Test
    void testGetTopTenHighestEarningEmployeeNames_Success() {
        List<String> topEmployees = Arrays.asList("Devki", "Chauhan", "Pooja", "ABC", "BCD", "test123");
        when(employeeService.fetchTopTenHighestEarningEmployeeNames()).thenReturn(topEmployees);

        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(6, response.getBody().size());
    }

    @Test
    void testCreateEmployee_Success() {
        EmployeeDTO employeeDTO = new EmployeeDTO("Devki", 5000, 35, "Staff Engg", "dev123@test.com");
        when(employeeService.saveEmployee(employeeDTO)).thenReturn(employees.get(0));

        ResponseEntity<Employee> response = employeeController.createEmployee(employeeDTO);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Devki", response.getBody().getEmployee_name());
    }

    @Test
    void testCreateEmployee_BadRequest() {
        EmployeeDTO employeeDTO = new EmployeeDTO("Devki", 5000, 8, "Staff Engg", "dev123@test.com");
        when(employeeService.saveEmployee(employeeDTO))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Age should be minimum 16"));

        HttpClientErrorException exception =
                assertThrows(HttpClientErrorException.class, () -> employeeController.createEmployee(employeeDTO));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testDeleteEmployeeById_Success() {
        when(employeeService.deleteEmployeeById("1")).thenReturn("Employee deleted successfully");

        ResponseEntity<String> response = employeeController.deleteEmployeeById("1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Employee deleted successfully", response.getBody());
    }

    @Test
    void testDeleteEmployeeById_Failure() {
        when(employeeService.deleteEmployeeById("2")).thenThrow(new RuntimeException("Employee not found"));

        Exception exception = assertThrows(RuntimeException.class, () -> employeeController.deleteEmployeeById("2"));
        assertEquals("Employee not found", exception.getMessage());
    }
}
