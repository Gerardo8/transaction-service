package com.spin.transactions.infrastructure.adapter.out.external;

import org.springframework.test.web.client.MockRestServiceServer;

record TestClient(MockRestServiceServer server, ExternalProviderRestClientAdapter adapter) {
}
