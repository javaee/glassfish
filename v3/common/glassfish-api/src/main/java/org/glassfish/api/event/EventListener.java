package org.glassfish.api.event;

/**
 * User: Jerome Dochez
 * Date: May 22, 2008
 * Time: 4:35:19 PM
 */
public interface EventListener {

    public void event(Event event);

    public class Event {
        final long inception;
        final String type;
        public Event(String type) {
            inception = System.currentTimeMillis();
            this.type = type;
        }
        public long inception() {
            return inception;
        }
    }

}
