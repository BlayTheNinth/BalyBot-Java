package net.blay09.balybot;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

import java.util.Map;

public class EventManager {

    private static final Map<String, EventBus> eventBusMap = Maps.newHashMap();

    public static EventBus get(String name) {
        EventBus eventBus = eventBusMap.get(name);
        if(eventBus == null) {
            eventBus = new EventBus();
            eventBusMap.put(name, eventBus);
        }
        return eventBus;
    }

}
