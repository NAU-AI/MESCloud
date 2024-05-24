package com.alcegory.mescloud.service.kpi;

import com.alcegory.mescloud.model.dto.kpi.KpiDto;
import com.alcegory.mescloud.model.filter.FilterDto;

public interface QualityKpiService {

    KpiDto computeQuality(Long equipmentId, FilterDto filter);
}
