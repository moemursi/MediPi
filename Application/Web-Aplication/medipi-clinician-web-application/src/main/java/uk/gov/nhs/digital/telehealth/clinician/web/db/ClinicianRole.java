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
package uk.gov.nhs.digital.telehealth.clinician.web.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "clinician_role")
public class ClinicianRole {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "clinician_role_id")
	private Long clinicianRoleId;

	@Column(name = "clinician_uuid")
	private String clinicianId;

	@Column(name = "role")
	private String role;

	public String getRole() {
		return role;
	}

	public void setRole(final String role) {
		this.role = role;
	}

	public String getClinicianId() {
		return clinicianId;
	}

	public void setClinicianId(final String clinicianId) {
		this.clinicianId = clinicianId;
	}

	public Long getClinicianRoleId() {
		return clinicianRoleId;
	}

	public void setClinicianRoleId(final Long clinicianRoleId) {
		this.clinicianRoleId = clinicianRoleId;
	}
}