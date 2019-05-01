package ru.ifmo.se.s267880.lab56.server.commandHandlers;

import ru.ifmo.se.s267880.lab56.server.Services;
import ru.ifmo.se.s267880.lab56.server.UserState;
import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.ZoneUtils;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.MiscellaneousCommandHandlers;

import java.time.ZoneId;
import java.util.Map;
import java.util.function.Supplier;

public class ServerMiscellaneousCommandHandlers extends ServerCommandHandlers
    implements MiscellaneousCommandHandlers
{
    public ServerMiscellaneousCommandHandlers(Services services) {
        super(services);
    }

    /**
     * show file name, number of meeting and the time the file first load during this session.
     */
    @Override
    public synchronized void info(HandlerCallback<Map<String, String>> callback) {
        try {
            callback.onSuccess(services.getUserState().generateInfo());
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void listTimeZones(int offsetHour, HandlerCallback<Map<Integer, ZoneId>> callback) {
        callback.onSuccess(ZoneUtils.getZonesBy(z -> ZoneUtils.toUTCZoneOffset(z).getTotalSeconds() / 3600 == offsetHour));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setTimeZone(int timeZoneKey, HandlerCallback callback) {
        if (!ZoneUtils.allZoneIds.containsKey(timeZoneKey)) {
            callback.onError(new NoSuchFieldException(String.format(
                    "There is no time zones with index %d. " +
                            "Please use command `list-time-zones` for the list of time zones", timeZoneKey)));
            return;
        }
        services.getUserState().setTimeZone(ZoneUtils.allZoneIds.get(timeZoneKey));
        callback.onSuccess(null);
    }
}
