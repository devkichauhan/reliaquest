package com.reliaquest.api.service;

import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.dto.ResponseDTO;
import com.reliaquest.api.entity.Employee;
import com.reliaquest.api.util.ResponseUtil;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private static final String TEST_DATA_API_URL = "http://localhost:8112/api/v1/employee";
    public static final String URL_SEPARATOR = "/";
    private final RestTemplate restTemplate;

    public List<Employee> fetchAllEmployees() {
        ResponseDTO<List<Employee>> response = restTemplate.getForObject(TEST_DATA_API_URL, ResponseDTO.class);
        List<Employee> employees = ResponseUtil.extractListData(response, Employee.class);
        log.info("Total employees: {}", employees.size());
        return employees;
    }

    public Employee fetchEmployeeById(String employeeId) {
        String getEmployeeByIdUrl = TEST_DATA_API_URL + URL_SEPARATOR + employeeId;
        ResponseDTO<Employee> response = restTemplate.getForObject(getEmployeeByIdUrl, ResponseDTO.class);
        Employee employee = ResponseUtil.extractData(response, Employee.class);
        log.info("Fetched employee: {}", employee);
        return employee;
    }

    public List<Employee> findEmployeesByNameMatchesOrContains(String nameToSearch) {
        List<Employee> employees = fetchAllEmployees();
        List<Employee> matchedEmployees = employees.stream()
                .filter(employee -> employee.getEmployee_name().toLowerCase().contains(nameToSearch.toLowerCase()))
                .toList();
        log.info("Found {} employees matching name: {}", matchedEmployees.size(), nameToSearch);
        return matchedEmployees;
    }

    public Integer fetchHighestSalaryAmongAllEmployees() {
        List<Employee> employees = fetchAllEmployees();
        int highestSalary =
                employees.stream().mapToInt(Employee::getEmployee_salary).max().orElse(0);
        log.info("Highest salary: {}", highestSalary);
        return highestSalary;
    }

    public List<String> fetchTopTenHighestEarningEmployeeNames() {
        List<Employee> employees = fetchAllEmployees();

        PriorityQueue<Employee> priorityQueue =
                new PriorityQueue<>(Comparator.comparingInt(Employee::getEmployee_salary));
        for (Employee employee : employees) {
            priorityQueue.offer(employee);
            if (priorityQueue.size() > 10) {
                priorityQueue.poll();
            }
        }

        List<String> topTenEarners = priorityQueue.stream()
                .sorted(Comparator.comparingInt(Employee::getEmployee_salary).reversed())
                .map(Employee::getEmployee_name)
                .toList();

        log.info("Top 10 earners: {}", topTenEarners);
        return topTenEarners;
    }

    public Employee saveEmployee(EmployeeDTO employeeDTO) {
        ResponseDTO<Employee> response = restTemplate.postForObject(TEST_DATA_API_URL, employeeDTO, ResponseDTO.class);
        Employee employee = ResponseUtil.extractData(response, Employee.class);
        log.info("Saved employee: {}", employee);
        return employee;
    }

    public String deleteEmployeeById(String employeeId) {
        restTemplate.delete(TEST_DATA_API_URL + URL_SEPARATOR + employeeId);
        log.info("Employee with ID: {} deleted successfully", employeeId);
        return "Employee deleted successfully";
    }
}
