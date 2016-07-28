/*
 *
 * Copyright (C) 2016 Krishna Kuntala @ Mastek <krishna.kuntala@mastek.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.gov.nhs.digital.telehealth.clinician.service.dao.impl;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Service;

import uk.gov.nhs.digital.telehealth.clinician.service.entities.DataValueEntity;
import uk.gov.nhs.digital.telehealth.clinician.service.entities.RecordingDeviceDataMaster;

import com.dev.ops.common.dao.generic.GenericDAOImpl;
import com.dev.ops.common.domain.ContextInfo;

@Service
public class RecordingDeviceDataDAOImpl extends GenericDAOImpl<RecordingDeviceDataMaster> implements RecordingDeviceDataDAO {

	private static String FETCH_RECENT_MEASUREMENTS_NATIVE_POSTGRESQL_QUERY;

	static {
		StringBuilder query = new StringBuilder();
		query.append("SELECT rdd.data_id as \"data_id\", rdt.type as \"reading_type\", rdt.subtype as \"device\", rda.attribute_name as \"attribute_name\", rdd.data_value as \"data\", rdt.type_id as \"type_id\", rdd.data_value_time as \"data_time\", rdd.downloaded_time as \"submitted_time\"");
		query.append(" FROM recording_device_data rdd");
		query.append(" JOIN recording_device_attribute rda ON rdd.attribute_id = rda.attribute_id");
		query.append(" JOIN recording_device_type rdt ON rda.type_id = rdt.type_id");
		query.append(" JOIN  (SELECT MAX(rddd.data_value_time) as data_value_time, rddd.attribute_id as attribute_id FROM recording_device_data rddd");
		query.append(" WHERE patient_id = :patientId");
		query.append(" GROUP BY rddd.attribute_id) latest_device_data ON rdd.attribute_id = latest_device_data.attribute_id AND rdd.data_value_time = latest_device_data.data_value_time");
		query.append(" WHERE rdd.patient_id = :patientId");
		query.append(" ORDER BY rdt.type_id ASC");

		FETCH_RECENT_MEASUREMENTS_NATIVE_POSTGRESQL_QUERY = query.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RecordingDeviceDataMaster> fetchRecentMeasurementsHQL(final String patientId, final ContextInfo contextInfo) {
		final Query query = this.getEntityManager().createNamedQuery("RecordingDeviceDataMaster.fetchRecentMeasurements", RecordingDeviceDataMaster.class);
		query.setParameter("patientId", patientId);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DataValueEntity> fetchRecentMeasurementsSQL(final String patientId, final ContextInfo contextInfo) {
		final Query query = this.getEntityManager().createNativeQuery(FETCH_RECENT_MEASUREMENTS_NATIVE_POSTGRESQL_QUERY, DataValueEntity.class);
		query.setParameter("patientId", patientId);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RecordingDeviceDataMaster> fetchPatientMeasurementsByAttributeId(final String patientId, final int attributeId) {
		final Query query = this.getEntityManager().createNamedQuery("RecordingDeviceDataMaster.fetchMeasurements", RecordingDeviceDataMaster.class);
		query.setParameter("patientId", patientId);
		query.setParameter("attributeId", attributeId);
		return query.getResultList();
	}
}