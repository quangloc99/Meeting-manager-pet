package ru.ifmo.se.s267880.lab56.server.commandHandlers;

import ru.ifmo.se.s267880.lab56.server.services.Services;
import ru.ifmo.se.s267880.lab56.shared.HandlerCallback;
import ru.ifmo.se.s267880.lab56.shared.ZoneUtils;
import ru.ifmo.se.s267880.lab56.shared.sharedCommandHandlers.MiscellaneousCommandHandlers;

import java.time.ZoneId;
import java.util.Map;
import java.util.TimeZone;

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
    public void setTimeZone(String zoneId, HandlerCallback callback) {
        if (!ZoneUtils.isValidTimeZone(zoneId)) {
            callback.onError(new Exception("Incorrect time zone!"));
        }
        services.getUserState().setTimeZone(TimeZone.getTimeZone(zoneId).toZoneId());
        callback.onSuccess(null);
    }
}
