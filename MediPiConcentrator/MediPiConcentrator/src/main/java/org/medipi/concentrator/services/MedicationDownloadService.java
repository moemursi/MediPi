package org.medipi.concentrator.services;

import org.medipi.concentrator.dao.*;
import org.medipi.concentrator.exception.NotFound404Exception;
import org.medipi.concentrator.logic.AdherenceCalculator;
import org.medipi.concentrator.utilities.Utilities;
import org.medipi.medication.PatientAdherence;
import org.medipi.medication.RecordedDose;
import org.medipi.medication.Schedule;
import org.medipi.medication.ScheduledDose;
import org.medipi.model.MedicationDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class MedicationDownloadService {

    @Autowired
    private Utilities utils;

    @Autowired
    private PatientDeviceValidationService patientDeviceValidationService;

    @Autowired
    private ScheduleDAOImpl scheduleDAOimpl;

    @Autowired
    private PatientAdherenceDAOImpl patientAdherenceDAOimpl;

    @Autowired
    private ScheduleAdherenceDAOImpl scheduleAdherenceDAOimpl;

    @Autowired
    private RecordedDoseDAOImpl recordedDoseDAOimpl;

    @Transactional(rollbackFor = RuntimeException.class)
    public ResponseEntity<MedicationDO> getMedicationData(String patientUuid) {

        MedicationDO medicationInfo = new MedicationDO();
        List<ScheduledDose> doses = new ArrayList<>();
        System.out.println(org.hibernate.Version.getVersionString());
        try {
            medicationInfo.setSchedules(scheduleDAOimpl.findAll());
            for (Schedule schedule: medicationInfo.getSchedules()) {
                System.out.println(schedule.getAssignedStartDate());
            }
            medicationInfo.setPatientAdherence(patientAdherenceDAOimpl.findByPatientUuid(patientUuid));

        } catch (Exception e) {
            System.out.println(String.format("Failed to process query results (or none returned). Error was %s: %s", e.getClass(), e.getMessage()));
        }
        try {
            return new ResponseEntity<MedicationDO>(medicationInfo, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(String.format("Creation of ResponseEntity failed. Error was %s: %s", e.getClass(), e.getMessage()));
        }
        return new ResponseEntity<MedicationDO>(medicationInfo, HttpStatus.OK);
    }

    @Transactional(rollbackFor = Throwable.class)
    private void uploadRecordedDoses(MedicationDO uploadedData) {
        List<RecordedDose> doseData = new ArrayList<>();
        for (Schedule schedule: uploadedData.getSchedules()) {
            doseData.addAll(schedule.getRecordedDoses());
        }
        System.out.println("RECEIVED UPLOAD: " + " " + doseData.size());
        for (RecordedDose dose: doseData) {
            try{recordedDoseDAOimpl.findByRecordedDoseUUID(dose.getRecordedDoseUUID());
            } catch (EmptyResultDataAccessException e) {
                recordedDoseDAOimpl.save(dose);
            }
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public ResponseEntity<MedicationDO> synchronize(MedicationDO uploadedData) {
        assert patientDeviceValidationService != null;
        String patientUuid = uploadedData.getPatientUuid();
        String hardwareName = uploadedData.getHardwareName();
        try {
            // Check that the device and patient are registered with each other
            this.patientDeviceValidationService.validate(hardwareName, patientUuid);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new NotFound404Exception("Hardware and/or patient not registered" + e.getLocalizedMessage());
        }
        uploadRecordedDoses(uploadedData);
        updateAdherence(patientUuid);
        return getMedicationData(patientUuid);
    }

    @Transactional(rollbackFor = Throwable.class)
    private void updateAdherence(String patientUuid) {
        LocalDate searchEndTime = LocalDate.now();
        LocalDate searchStartTime = searchEndTime.minusDays(7);
        Collection<Schedule> patientSchedules = scheduleDAOimpl.findByPatientUuid(patientUuid);
        AdherenceCalculator adherenceCalculator = new AdherenceCalculator(patientSchedules, searchStartTime, searchEndTime, true);
        for (Schedule schedule: patientSchedules) {
            schedule.getScheduleAdherence().setSevenDayFraction(adherenceCalculator.getAdherenceFractionOf(schedule));
            schedule.getScheduleAdherence().setStreakLength(adherenceCalculator.getStreakLengthOf(schedule));
            scheduleAdherenceDAOimpl.update(schedule.getScheduleAdherence());
        }
        PatientAdherence patientAdherence = patientAdherenceDAOimpl.findByPatientUuid(patientUuid);
        patientAdherence.setSevenDayFraction(adherenceCalculator.getPatientFraction());
        patientAdherence.setStreakLength(adherenceCalculator.getPatientStreaklength());
        patientAdherenceDAOimpl.update(patientAdherence);
    }


    @Transactional(rollbackFor = Throwable.class)
    public ResponseEntity<MedicationDO> getRegisteredPatients(MedicationDO uploadedData) {
        assert patientDeviceValidationService != null;
        String patientUuid = uploadedData.getPatientUuid();
        String hardwareName = uploadedData.getHardwareName();
        try {
            // Check that the device and patient are registered with each other
            this.patientDeviceValidationService.validate(hardwareName, patientUuid);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new NotFound404Exception("Hardware and/or patient not registered" + e.getLocalizedMessage());
        }
        uploadRecordedDoses(uploadedData);
        updateAdherence(patientUuid);
        return getMedicationData(patientUuid);
    }
}