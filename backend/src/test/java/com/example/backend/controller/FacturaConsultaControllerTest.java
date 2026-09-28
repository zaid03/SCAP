package com.example.backend.controller;

import com.example.backend.config.TestSecurityConfig;
import com.example.backend.config.TestExceptionHandler;
import com.example.backend.dto.FacturaConsultaRequestDto;
import com.example.backend.exception.XmlParsingException;
import com.example.backend.service.FacturaConsultaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FacturaConsultaController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestExceptionHandler.class})
public class FacturaConsultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FacturaConsultaService facturaConsultaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void consultaFacturas_returns200WithFacturas() throws Exception {
        String smlInput = "<sml>test input</sml>";
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;-1&lt;/exito&gt;
                    &lt;factura&gt;
                      &lt;numero&gt;FAC001&lt;/numero&gt;
                      &lt;monto&gt;100.00&lt;/monto&gt;
                    &lt;/factura&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        when(facturaConsultaService.buildSmlInput(any(FacturaConsultaRequestDto.class)))
            .thenReturn(smlInput);
        when(facturaConsultaService.sendSmlRequest(smlInput, "http://ws.url"))
            .thenReturn(soapResponse);

        String requestJson = objectMapper.writeValueAsString(Map.of(
            "webserviceUrl", "http://ws.url"
        ));

        mockMvc.perform(post("/api/facturas/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].numero").value("FAC001"))
            .andExpect(jsonPath("$[0].monto").value("100.00"));
    }

    @Test
    void consultaFacturas_returns404WhenNoFacturas() throws Exception {
        String smlInput = "<sml>test input</sml>";
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;-1&lt;/exito&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        when(facturaConsultaService.buildSmlInput(any(FacturaConsultaRequestDto.class)))
            .thenReturn(smlInput);
        when(facturaConsultaService.sendSmlRequest(smlInput, "http://ws.url"))
            .thenReturn(soapResponse);

        String requestJson = objectMapper.writeValueAsString(Map.of(
            "webserviceUrl", "http://ws.url"
        ));

        mockMvc.perform(post("/api/facturas/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));
    }

    @Test
    void consultaFacturas_returns400OnBadResponse() throws Exception {
        String smlInput = "<sml>test input</sml>";
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;0&lt;/exito&gt;
                    &lt;desc&gt;Factura no encontrada&lt;/desc&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        when(facturaConsultaService.buildSmlInput(any(FacturaConsultaRequestDto.class)))
            .thenReturn(smlInput);
        when(facturaConsultaService.sendSmlRequest(smlInput, "http://ws.url"))
            .thenReturn(soapResponse);

        String requestJson = objectMapper.writeValueAsString(Map.of(
            "webserviceUrl", "http://ws.url"
        ));

        mockMvc.perform(post("/api/facturas/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Factura no encontrada"));
    }

    @Test
    void consultaFacturas_returns500OnInvalidSoap() throws Exception {
        String smlInput = "<sml>test input</sml>";
        String invalidSoapResponse = "not valid soap";

        when(facturaConsultaService.buildSmlInput(any(FacturaConsultaRequestDto.class)))
            .thenReturn(smlInput);
        when(facturaConsultaService.sendSmlRequest(smlInput, "http://ws.url"))
            .thenReturn(invalidSoapResponse);

        String requestJson = objectMapper.writeValueAsString(Map.of(
            "webserviceUrl", "http://ws.url"
        ));

        mockMvc.perform(post("/api/facturas/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Respuesta SOAP inválida"));
    }

    @Test
    void consultaFacturas_returns500OnXmlParsingException() throws Exception {
        String smlInput = "<sml>test input</sml>";
        String malformedSoapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;-1&lt;/exito&gt;
                    &lt;invalid xml here
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        when(facturaConsultaService.buildSmlInput(any(FacturaConsultaRequestDto.class)))
            .thenReturn(smlInput);
        when(facturaConsultaService.sendSmlRequest(smlInput, "http://ws.url"))
            .thenReturn(malformedSoapResponse);

        String requestJson = objectMapper.writeValueAsString(Map.of(
            "webserviceUrl", "http://ws.url"
        ));

        mockMvc.perform(post("/api/facturas/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("XML parsing error")));
    }

    @Test
    void consultaFacturas_returns500OnServiceException() throws Exception {
        when(facturaConsultaService.buildSmlInput(any(FacturaConsultaRequestDto.class)))
            .thenThrow(new RuntimeException("Service error"));

        String requestJson = objectMapper.writeValueAsString(Map.of(
            "webserviceUrl", "http://ws.url"
        ));

        mockMvc.perform(post("/api/facturas/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("Error:")));
    }

    @Test
    void consultaFacturas_returns200WithMultipleFacturas() throws Exception {
        String smlInput = "<sml>test input</sml>";
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;-1&lt;/exito&gt;
                    &lt;factura&gt;
                      &lt;numero&gt;FAC001&lt;/numero&gt;
                      &lt;monto&gt;100.00&lt;/monto&gt;
                    &lt;/factura&gt;
                    &lt;factura&gt;
                      &lt;numero&gt;FAC002&lt;/numero&gt;
                      &lt;monto&gt;200.00&lt;/monto&gt;
                    &lt;/factura&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        when(facturaConsultaService.buildSmlInput(any(FacturaConsultaRequestDto.class)))
            .thenReturn(smlInput);
        when(facturaConsultaService.sendSmlRequest(smlInput, "http://ws.url"))
            .thenReturn(soapResponse);

        String requestJson = objectMapper.writeValueAsString(Map.of(
            "webserviceUrl", "http://ws.url"
        ));

        mockMvc.perform(post("/api/facturas/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].numero").value("FAC001"))
            .andExpect(jsonPath("$[1].numero").value("FAC002"))
            .andExpect(jsonPath("$[1].monto").value("200.00"));
    }

    @Test
    void consultaFacturas_returns400WhenDescIsNull() throws Exception {
        String smlInput = "<sml>test input</sml>";
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;1&lt;/exito&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        when(facturaConsultaService.buildSmlInput(any(FacturaConsultaRequestDto.class)))
            .thenReturn(smlInput);
        when(facturaConsultaService.sendSmlRequest(smlInput, "http://ws.url"))
            .thenReturn(soapResponse);

        String requestJson = objectMapper.writeValueAsString(Map.of(
            "webserviceUrl", "http://ws.url"
        ));

        mockMvc.perform(post("/api/facturas/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Error desconocido"));
    }

    @Test
    void consultaFacturas_returns500WhenSoapIsNull() throws Exception {
        String smlInput = "<sml>test input</sml>";

        when(facturaConsultaService.buildSmlInput(any(FacturaConsultaRequestDto.class)))
            .thenReturn(smlInput);
        when(facturaConsultaService.sendSmlRequest(smlInput, "http://ws.url"))
            .thenReturn(null);

        String requestJson = objectMapper.writeValueAsString(Map.of(
            "webserviceUrl", "http://ws.url"
        ));

        mockMvc.perform(post("/api/facturas/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Respuesta SOAP inválida"));
    }

    @Test
    void consultaFacturas_returns500WhenSoapIsBlank() throws Exception {
        String smlInput = "<sml>test input</sml>";

        when(facturaConsultaService.buildSmlInput(any(FacturaConsultaRequestDto.class)))
            .thenReturn(smlInput);
        when(facturaConsultaService.sendSmlRequest(smlInput, "http://ws.url"))
            .thenReturn("   ");

        String requestJson = objectMapper.writeValueAsString(Map.of(
            "webserviceUrl", "http://ws.url"
        ));

        mockMvc.perform(post("/api/facturas/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Respuesta SOAP inválida"));
    }

    @Test
    void consultaFacturas_returns500WhenServicioReturnMissing() throws Exception {
        String smlInput = "<sml>test input</sml>";
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <invalidTag>content</invalidTag>
              </soap:Body>
            </soap:Envelope>
            """;

        when(facturaConsultaService.buildSmlInput(any(FacturaConsultaRequestDto.class)))
            .thenReturn(smlInput);
        when(facturaConsultaService.sendSmlRequest(smlInput, "http://ws.url"))
            .thenReturn(soapResponse);

        String requestJson = objectMapper.writeValueAsString(Map.of(
            "webserviceUrl", "http://ws.url"
        ));

        mockMvc.perform(post("/api/facturas/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Respuesta SOAP inválida"));
    }

    @Test
    void consultaFacturas_returns500WhenServicioReturnMalformed() throws Exception {
        String smlInput = "<sml>test input</sml>";
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn></servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        when(facturaConsultaService.buildSmlInput(any(FacturaConsultaRequestDto.class)))
            .thenReturn(smlInput);
        when(facturaConsultaService.sendSmlRequest(smlInput, "http://ws.url"))
            .thenReturn(soapResponse);

        String requestJson = objectMapper.writeValueAsString(Map.of(
            "webserviceUrl", "http://ws.url"
        ));

        mockMvc.perform(post("/api/facturas/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("XML parsing error")));
    }

    @Test
    void consultaFacturas_returns400WhenExitoHasNoTextContent() throws Exception {
        String smlInput = "<sml>test input</sml>";
        String soapResponse = """
            <soap:Envelope>
              <soap:Body>
                <servicioReturn>
                  &lt;response&gt;
                    &lt;exito&gt;&lt;/exito&gt;
                    &lt;desc&gt;Error description&lt;/desc&gt;
                  &lt;/response&gt;
                </servicioReturn>
              </soap:Body>
            </soap:Envelope>
            """;

        when(facturaConsultaService.buildSmlInput(any(FacturaConsultaRequestDto.class)))
            .thenReturn(smlInput);
        when(facturaConsultaService.sendSmlRequest(smlInput, "http://ws.url"))
            .thenReturn(soapResponse);

        String requestJson = objectMapper.writeValueAsString(Map.of(
            "webserviceUrl", "http://ws.url"
        ));

        mockMvc.perform(post("/api/facturas/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Error description"));
    }
}