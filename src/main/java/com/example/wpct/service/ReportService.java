package com.example.wpct.service;

import com.example.wpct.utils.ResultBody;

public interface ReportService {
    ResultBody getReport(String startDate, String endDate, String villageName);

}
