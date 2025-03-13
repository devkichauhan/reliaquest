package com.reliaquest.api.controller;

import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.entity.Employee;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<Employee, EmployeeDTO> {

    private final EmployeeService employeeService;

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("Fetching all employees");
        List<Employee> employees = employeeService.fetchAllEmployees();
        return employees.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        log.info("Searching all employees whose name contains or matches: {}", searchString);
        List<Employee> matchedEmployees = employeeService.findEmployeesByNameMatchesOrContains(searchString);
        return matchedEmployees.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(matchedEmployees);
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        log.info("Fetching employee by employeeId: {}", id);
        return ResponseEntity.ok(employeeService.fetchEmployeeById(id));
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("Fetching highest salary among all employees");
        return ResponseEntity.ok(employeeService.fetchHighestSalaryAmongAllEmployees());
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("Fetching top 10 highest earning employee names");
        List<String> topTenEarners = employeeService.fetchTopTenHighestEarningEmployeeNames();
        return topTenEarners.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(topTenEarners);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@Valid EmployeeDTO employeeDTO) {
        log.info("Creating new employee: {}", employeeDTO.getName());
        Employee employee = employeeService.saveEmployee(employeeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        log.info("Deleting employee having employeeId: {}", id);
        String result = employeeService.deleteEmployeeById(id);
        return result.equalsIgnoreCase("Employee deleted successfully")
                ? ResponseEntity.ok(result)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }
}
