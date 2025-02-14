package com.lpdev.dsd.services.impl;

import com.lpdev.dsd.components.IPLogMapper;
import com.lpdev.dsd.models.dtos.IPLog;
import com.lpdev.dsd.repositories.IPLogRepository;
import com.lpdev.dsd.services.IPLogService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IPLogServiceImpl implements IPLogService {
  @Autowired private IPLogRepository ipLogRepository;

  @Autowired private IPLogMapper ipLogMapper;

  @Override
  public IPLog save(IPLog ipLog) {
    return Optional.of(ipLog)
        .map(ipLogMapper::toEntity)
        .map(ipLogRepository::save)
        .map(ipLogMapper::toDTO)
        .orElse(null);
  }
}
