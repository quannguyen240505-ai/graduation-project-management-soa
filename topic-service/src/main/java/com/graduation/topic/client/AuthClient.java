package com.graduation.topic.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthClient {

    private final RestClient restClient;

    public AuthClient(
            @Qualifier("authRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public UserInfo getUserById(Long id, String token) {
        try {
            UserInfo user = restClient.get()
                    .uri("/api/auth/users/{id}", id)
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .body(UserInfo.class);

            if (user == null
                    || !id.equals(user.id())
                    || user.role() == null
                    || user.status() == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Auth Service trả về thông tin không hợp lệ");
            }

            return user;

        } catch (RestClientResponseException ex) {
            int status = ex.getStatusCode().value();

            if (status == 404) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Người dùng có ID " + id + " không tồn tại",
                        ex);
            }

            if (status == 401 || status == 403) {
                throw new ResponseStatusException(
                        ex.getStatusCode(),
                        "Auth Service từ chối yêu cầu kiểm tra người dùng",
                        ex);
            }

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Auth Service đang gặp lỗi, vui lòng thử lại sau",
                    ex);

        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Không thể kết nối Auth Service",
                    ex);
        }
    }

    public record UserInfo(Long id, String role, String status) {
    }
}