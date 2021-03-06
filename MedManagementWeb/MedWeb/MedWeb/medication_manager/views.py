import datetime
import json

from django.http import HttpResponse
from django.template.loader import get_template

from MedWeb import settings
from MedWeb.clinical_database.clinical_database import medications
from MedWeb.clinical_judgements.schedule_logic import patient_active_schedules, patient_active_medication_ids
from MedWeb.concentrator_interface.interface import schedules
from MedWeb.concentrator_interface.interface import update_from_concentrator, get_patient_dose_instances
from MedWeb.patient_database.patient_database import patients

sidebar_menu_urls = {"Medications": "/viewpatient",
                     "Schedule & History": "/schedule",
                     "Add Medication": "/assignmedication"}

sidebar_menu_entries = ["Medications", "Schedule & History", "Add Medication"]


def browse_patients(request):
    template = get_template("medication/browse_patients.djt.html")
    output = template.render({"title": settings.SITE_NAME,
                              "version": settings.version_string,
                              "sections": settings.SECTIONS,
                              "sidebar_menu_urls": sidebar_menu_urls,
                              "active_section": "patients",
                              "sidebar_menu_entries": sidebar_menu_entries,
                              "patients": patients.values(),
                              "warning_count": sum((patient.warnings for patient in patients.values())),
                              "active_patient": None}, request)
    return HttpResponse(output)


def patient_summary(request):
    patient_uuid = request.GET.get("patient_uuid", None)
    todayDate = datetime.date.today()
    if patient_uuid is None:
        return browse_patients(request)
    print(patients[patient_uuid].status)
    template = get_template("medication/medication_summary.djt.html")
    dose_instances = get_patient_dose_instances(patient_uuid, todayDate - datetime.timedelta(
                                  todayDate.weekday()), todayDate - datetime.timedelta(
                                  todayDate.weekday()) + datetime.timedelta(days=7))
    for dose_instance in dose_instances:
        dose_date = dose_instance.schedule.start_date + datetime.timedelta(days=dose_instance.day)
        dose_instance.start_datetime = datetime.datetime.combine(dose_date, dose_instance.start_time)
        dose_instance.end_datetime = datetime.datetime.combine(dose_date, dose_instance.end_time)
        if dose_instance.start_datetime > datetime.datetime.now():
            dose_instance.color = "#ADECFF"
        elif dose_instance.taken_dose_uuid is not None:
            dose_instance.color ="#8BFF8E"
        else:
            dose_instance.color = "#FF8B8E"

    patient_schedules = list(patients[patient_uuid].schedules)
    patient_schedules.sort(key=lambda s: s.medication.short_name)
    patient_schedules.sort(key=lambda s: s.start_date)
    dose_instances.sort(key=lambda d: d.start_datetime)
    dose_instances.sort(key=lambda d: d.schedule.medication.full_name)
    print([schedule for schedule in patient_schedules])
    output = template.render({"title": settings.SITE_NAME,
                              "version": settings.version_string,
                              "sections": settings.SECTIONS,
                              "sidebar_menu_entries": sidebar_menu_entries,
                              "sidebar_menu_urls": sidebar_menu_urls,
                              "active_sidebar_entry": "Medications",
                              "active_section": "patients",
                              "active_patient": patients[patient_uuid],
                              "dose_instances": dose_instances,
                              "schedules": patient_schedules}, request)
    return HttpResponse(output)


def assign_medication(request):
    patient_uuid = request.GET.get("patient_uuid", None)
    if patient_uuid is None:
        return browse_patients(request)
    template = get_template("medication/assign_medication.djt.html")
    active_medications = patient_active_medication_ids(patients[patient_uuid])
    print(active_medications)
    output = template.render({"title": settings.SITE_NAME,
                              "version": settings.version_string,
                              "sections": settings.SECTIONS,
                              "sidebar_menu_entries": sidebar_menu_entries,
                              "sidebar_menu_urls": sidebar_menu_urls,
                              "active_sidebar_entry": "Add Medication",
                              "active_section": "patients",
                              "active_patient": patients[patient_uuid],
                              "active_patient_medication_ids": json.dumps(
                                  active_medications),
                              "medications": medications.values(),
                              }, request)
    return HttpResponse(output)


def create_new_schedule(request):
    patient_uuid = request.GET.get("patient_uuid", None)
    if patient_uuid is None:
        return browse_patients(request)
    medication_id_str = request.GET.get("mid", None)
    if medication_id_str is None:
        return assign_medication(request)
    medication_id = int(medication_id_str)
    template = get_template("medication/schedule_editor.html")
    output = template.render({"title": settings.SITE_NAME,
                              "version": settings.version_string,
                              "sections": settings.SECTIONS,
                              "active_section": "patients",
                              "sidebar_menu_entries": sidebar_menu_entries,
                              "sidebar_menu_urls": sidebar_menu_urls,
                              "active_sidebar_entry": "Add Medication",
                              "active_patient": patients[patient_uuid],
                              "active_medication": medications[medication_id],
                              "mode": 'create'}, request)
    return HttpResponse(output)


def modify_schedule(request):
    schedule_id_str = request.GET.get("schedule_id", None)
    if schedule_id_str is None:
        return patient_summary(request)
    schedule_id = int(schedule_id_str)
    print(schedule_id)
    print(schedules)
    exclusive_for_this_medication = len([schedule for schedule in schedules[schedule_id].patient.schedules if schedule.medication_id == schedules[schedule_id].medication_id]) <= 1
    template = get_template("medication/schedule_editor.html")
    output = template.render({"title": settings.SITE_NAME,
                              "version": settings.version_string,
                              "active_section": "patients",
                              "sections": settings.SECTIONS,
                              "sidebar_menu_entries": sidebar_menu_entries,
                              "sidebar_menu_urls": sidebar_menu_urls,
                              "active_sidebar_entry": "Medications",
                              "active_patient": schedules[schedule_id].patient,
                              "active_schedule": schedules[schedule_id],
                              "active_medication": schedules[schedule_id].medication,
                              "mode": 'modify',
                              "enable_start_date_change": schedules[schedule_id].start_date > datetime.date.today() and exclusive_for_this_medication}, request)
    return HttpResponse(output)


def query_concentrator(request):
    update_from_concentrator()
