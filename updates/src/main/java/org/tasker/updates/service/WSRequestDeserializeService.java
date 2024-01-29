package org.tasker.updates.service;

import org.tasker.updates.models.request.WSRequest;

public interface WSRequestDeserializeService {
    WSRequest deserialize(byte[] bytes);
}
