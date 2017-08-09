package org.medipi.concentrator.dao;

import org.medipi.medication.RecordedDose;
import org.medipi.medication.ScheduledDose;

import java.util.List;

/**
 * Created by sam on 02/08/17.
 */
public interface RecordedDoseDAO extends GenericDAO<RecordedDose> {
   RecordedDose findByRecordedDoseUUID(String recordedDoseUUID);
    List<RecordedDose> findAll();
}