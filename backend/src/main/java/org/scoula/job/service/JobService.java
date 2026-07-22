package org.scoula.job.service;

import org.scoula.job.dto.JobCodeDTO;

import java.util.List;

public interface JobService {
    List<JobCodeDTO> findJobCodes(String goalType);
}
