package org.medipi.concentrator.logic;

import org.medipi.medication.ScheduledDose;

import java.sql.Time;

class DoseInstance {
    public ScheduledDose getDose() {
        return dose;
    }

    private ScheduledDose dose;

    public int getDay() {
        return day;
    }

    private int day;

    public Time getTimeStart() {
        return timeStart;
    }

    public Time getTimeEnd() {
        return timeEnd;
    }

    private Time timeStart;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DoseInstance that = (DoseInstance) o;

        if (day != that.day) return false;
        if (dose != null ? !dose.equals(that.dose) : that.dose != null) return false;
        if (timeStart != null ? !timeStart.equals(that.timeStart) : that.timeStart != null) return false;
        return timeEnd != null ? timeEnd.equals(that.timeEnd) : that.timeEnd == null;
    }

    private Time timeEnd;

    DoseInstance(ScheduledDose dose, int day) {
        this.dose = dose;
        this.day = day;
        this.timeStart = dose.getWindowStartTime();
        this.timeEnd = dose.getWindowEndTime();
    }

    public String toString() {
        return String.format("DoseInstance: Day %d from %s to %s", day, timeStart, timeEnd);
    }

}
