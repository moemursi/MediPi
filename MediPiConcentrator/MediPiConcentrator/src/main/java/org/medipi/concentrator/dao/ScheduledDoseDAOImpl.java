/*
 Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.concentrator.dao;

import org.medipi.medication.model.ScheduledDose;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data Access Object for Hardware
 *
 * @author rick@robinsonhq.com
 */
@Repository
public class ScheduledDoseDAOImpl extends GenericDAOImpl<ScheduledDose> implements ScheduledDoseDAO {
    @Override
    public ScheduledDose findByMedicationId(int scheduledDoseId) {
        return this.getEntityManager().createNamedQuery("ScheduledDose.findByScheduleId", ScheduledDose.class)
                .setParameter("id", scheduledDoseId)
                .getSingleResult();
    }

    @Override
    public List<ScheduledDose> findAll() {
        return this.getEntityManager().createNamedQuery("ScheduledDose.findAll", ScheduledDose.class)
                .getResultList();
    }
}
