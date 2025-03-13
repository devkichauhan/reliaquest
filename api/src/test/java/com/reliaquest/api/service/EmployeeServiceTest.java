package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.dto.ResponseDTO;
import com.reliaquest.api.entity.Employee;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

class EmployeeServiceTest {

    private static final String TEST_DATA_API_URL = "http://localhost:8112/api/v1/employee";

    private List<Employee> employees = new ArrayList<>();

    private ResponseDTO mockResponse = new ResponseDTO();

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employeeService = new EmployeeService(restTemplate);
        employees.add(new Employee("1", "Devki", 100, 30, "Engineer", "dev123@test.com"));
        employees.add(new Employee("2", "pooja", 200, 28, "Manager", "pooja123@test.com"));
    }

    @Test
    void testGetAllEmployees_Success() {
        mockResponse.setData(employees);
        when(restTemplate.getForObject(TEST_DATA_API_URL, ResponseDTO.class)).thenReturn(mockResponse);

        List<Employee> employees = employeeService.fetchAllEmployees();
        assertEquals(2, employees.size());
        assertEquals("Devki", employees.get(0).getEmployee_name());
        assertEquals(28, employees.get(1).getEmployee_age());
    }

    @Test
    void testGetAllEmployees_EmptyResponse() {
        mockResponse.setData(null);
        when(restTemplate.getForObject(TEST_DATA_API_URL, ResponseDTO.class)).thenReturn(mockResponse);

        List<Employee> employees = employeeService.fetchAllEmployees();
        assertTrue(employees.isEmpty());
    }

    @Test
    void testGetEmployeeById_Success() {
        mockResponse.setData(employees.get(0));
        when(restTemplate.getForObject(TEST_DATA_API_URL + "/1", ResponseDTO.class))
                .thenReturn(mockResponse);

        Employee employee = employeeService.fetchEmployeeById("1");
        assertNotNull(employee);
    }

    @Test
    void testGetEmployeeById_Error() {
        when(restTemplate.getForObject(TEST_DATA_API_URL + "/1", ResponseDTO.class))
                .thenThrow(new RuntimeException("Service Unavailable"));

        Exception exception = assertThrows(RuntimeException.class, () -> employeeService.fetchEmployeeById("1"));
        assertEquals("Service Unavailable", exception.getMessage());
    }

    @Test
    void testGetEmployeeById_NotFound() {
        when(restTemplate.getForObject(TEST_DATA_API_URL + "/2", ResponseDTO.class))
                .thenReturn(null);

        Employee employee = employeeService.fetchEmployeeById("2");
        assertNull(employee);
    }

    @Test
    void testGetHighestSalary_Success() {
        mockResponse.setData(employees);
        when(restTemplate.getForObject(TEST_DATA_API_URL, ResponseDTO.class)).thenReturn(mockResponse);

        int highestSalary = employeeService.fetchHighestSalaryAmongAllEmployees();
        assertEquals(200, highestSalary);
    }

    @Test
    void testGetHighestSalary_NoEmployees() {
        mockResponse.setData(new ArrayList<>());
        when(restTemplate.getForObject(TEST_DATA_API_URL, ResponseDTO.class)).thenReturn(mockResponse);

        int highestSalary = employeeService.fetchHighestSalaryAmongAllEmployees();
        assertEquals(0, highestSalary);
    }

    @Test
    void testGetTopTenHighestEarningEmployeeNames_Success() {
        mockResponse.setData(employees);
        when(restTemplate.getForObject(TEST_DATA_API_URL, ResponseDTO.class)).thenReturn(mockResponse);

        List<String> topEmployees = employeeService.fetchTopTenHighestEarningEmployeeNames();
        assertEquals(2, topEmployees.size());
        assertTrue(topEmployees.contains("pooja"));
    }

    @Test
    void testGetTopTenHighestEarningEmployees_LargeData() {
        List<Employee> employeeList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            employeeList.add(
                    new Employee(String.valueOf(i), "Employee" + i, 100 + i, 30, "test", "employee" + i + "@test.com"));
        }
        mockResponse.setData(employeeList);
        when(restTemplate.getForObject(TEST_DATA_API_URL, ResponseDTO.class)).thenReturn(mockResponse);

        List<String> topEmployees = employeeService.fetchTopTenHighestEarningEmployeeNames();
        assertEquals(10, topEmployees.size());
    }

    @Test
    void testCreateEmployee_Success() {
        mockResponse.setData(employees.get(0));
        EmployeeDTO employeeDTO = new EmployeeDTO("Devki", 100, 30, "Engineer", "dev123@test.com");

        when(restTemplate.postForObject(TEST_DATA_API_URL, employeeDTO, ResponseDTO.class))
                .thenReturn(mockResponse);

        Employee createdEmployee = employeeService.saveEmployee(employeeDTO);
        assertNotNull(createdEmployee);
        assertEquals("Devki", createdEmployee.getEmployee_name());
        assertEquals("Engineer", createdEmployee.getEmployee_title());
    }

    @Test
    void testCreateEmployee_Failure() {
        EmployeeDTO employeeDTO = new EmployeeDTO("Devki", 100, 30, "Engineer", "dev123@test.com");

        when(restTemplate.postForObject(TEST_DATA_API_URL, employeeDTO, ResponseDTO.class))
                .thenReturn(null);

        Employee createdEmployee = employeeService.saveEmployee(employeeDTO);
        assertNull(createdEmployee);
    }

    @Test
    void testDeleteEmployeeById_Success() {
        doNothing().when(restTemplate).delete(TEST_DATA_API_URL + "/1");

        String result = employeeService.deleteEmployeeById("1");
        assertEquals("Employee deleted successfully", result);
        verify(restTemplate, times(1)).delete(TEST_DATA_API_URL + "/1");
    }

    @Test
    void testDeleteEmployeeById_Failure() {
        doThrow(new RuntimeException("Delete failed")).when(restTemplate).delete(TEST_DATA_API_URL + "/2");

        Exception exception = assertThrows(RuntimeException.class, () -> employeeService.deleteEmployeeById("2"));
        assertEquals("Delete failed", exception.getMessage());
    }
}
