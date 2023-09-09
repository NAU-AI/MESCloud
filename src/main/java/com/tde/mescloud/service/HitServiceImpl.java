package com.tde.mescloud.service;

import com.tde.mescloud.model.converter.HitConverter;
import com.tde.mescloud.model.dto.HitDto;
import com.tde.mescloud.model.dto.RequestHitDto;
import com.tde.mescloud.model.entity.HitEntity;
import com.tde.mescloud.model.entity.SampleEntity;
import com.tde.mescloud.repository.HitRepository;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Log
public class HitServiceImpl implements HitService {

    public static final float TCA_LIMIT = 0.695f;

    private final SampleService sampleService;
    private final HitRepository repository;
    private final HitConverter converter;

    @Override
    public List<HitDto> create(RequestHitDto requestHitDto) {
        if (requestHitDto.getHits() == null || requestHitDto.getHits().isEmpty()) {
            throw new IllegalArgumentException("Hits list are not valid");
        }

        if (!areAllSampleIdsEqual(requestHitDto.getHits())) {
            throw new IllegalArgumentException("Not all Sample Ids are equal");
        }

        return saveHitsAndUpdateSample(requestHitDto.getHits());
    }

    private boolean areAllSampleIdsEqual(List<HitDto> hitDtoList) {
        if (hitDtoList == null || hitDtoList.isEmpty()) {
            return false;
        }

        Long firstSampleId = hitDtoList.get(0).getSampleId();
        for (HitDto hitDto : hitDtoList) {
            if (!firstSampleId.equals(hitDto.getSampleId())) {
                return false;
            }
        }

        return true;
    }

    private List<HitDto> saveHitsAndUpdateSample(List<HitDto> hitList) {
        SampleEntity sample = getSample(hitList);
        List<HitEntity> hits = converter.convertToEntity(hitList);

        saveHitAndSetSample(sample, hits);
        setTcaAverageInSample(sample, hits);
        setReliability(sample);

        sampleService.saveAndUpdate(sample);

        return converter.convertToDto(hits);
    }

    private void setReliability(SampleEntity sample) {
        List<HitEntity> validHitsAboveTcaLimit = repository.findValidHitsAboveTcaLimit(sample.getId(), TCA_LIMIT);
        double reliability = calculateSampleReliability(validHitsAboveTcaLimit.size(), sample.getAmount());
        sample.setReliability(reliability);
    }

    private double calculateSampleReliability(int numOfHitsAboveTcaLimit, int sampleAmount) {
        return (1 - (double) numOfHitsAboveTcaLimit / sampleAmount) * 100;
    }

    private void saveHitAndSetSample(SampleEntity sample, List<HitEntity> hits) {
        for (HitEntity hitEntity : hits) {
            hitEntity.setSample(sample);
        }
        saveAndUpdateAll(hits);
    }

    private void setTcaAverageInSample(SampleEntity sample, List<HitEntity> hits) {
        double tcaAverage = getTcaAverage(hits);
        sample.setTcaAverage(tcaAverage);
    }

    private double getTcaAverage(List<HitEntity> hits) {
        if (hits == null || hits.isEmpty()) {
            return 0.0;
        }

        float tcaSum = 0;
        for (HitEntity hit : hits) {
            tcaSum += hit.getTca();
        }

        return tcaSum / hits.size();
    }

    private SampleEntity getSample(List<HitDto> hitList) {
        HitDto hitDto = hitList.get(0);
        Optional<SampleEntity> sample = sampleService.findById(hitDto.getSampleId());
        if (sample.isEmpty()) {
            throw new IllegalArgumentException("Sample not found");
        }

        return sample.get();
    }

    public HitEntity saveAndUpdate(HitEntity hitEntity) {
        return repository.save(hitEntity);
    }

    @Override
    public List<HitEntity> saveAndUpdateAll(List<HitEntity> hitList) {
        return repository.saveAll(hitList);
    }

    @Override
    public void delete(HitEntity hitEntity) {
        repository.delete(hitEntity);
    }

    @Override
    public Optional<HitEntity> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<HitDto> getAll() {
        return converter.convertToDto(repository.findAll());
    }
}
