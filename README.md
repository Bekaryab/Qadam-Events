# Qadam Events

Qadam Events is an Android application that centralizes hackathons, IT events, conferences, workshops, and other events in one place. Users can discover, search, filter, register for events, communicate with other participants, and manage their event activity.

## Functionality

- Users can browse a centralized list of hackathons, IT events, conferences, workshops, and other events.
- Users can search and filter events by category, location, date, format, topic, and other criteria.
- Users can open an event and view its details, including description, date, location, organizer, and registration information.
- Users can register for an event and receive a QR code for event check-in.
- Organizers can scan a participant's QR code to confirm their attendance at the event.
- Users can save events to favorites and like, comment on, and share events with other users.
- Registered participants are automatically added to the event's group chat, where they can communicate with other participants and organizers.
- Users can create profiles and publish or view short videos (Reels) related to events and their activities.
- Only verified organizers can create and publish events, and submitted events must pass an internal verification process before publication.
- Organizers can manage their events, view registered participants, control the event group, and check participant attendance.

## Project Structure

```text
QadamEvents/
├── app/
│   ├── src/
│   │   ├── androidTest/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/bekaryab/qadamevents/
│   │   │   │       ├── MainActivity.kt
│   │   │   │       └── ui/theme/
│   │   │   ├── res/
│   │   │   ├── keepRules/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── .gitignore
└── README.md