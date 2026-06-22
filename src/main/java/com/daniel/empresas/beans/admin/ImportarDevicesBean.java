package com.daniel.empresas.beans.admin;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daniel.empresas.dto.response.DeviceImportResponseDTO;
import com.daniel.empresas.service.DeviceImportService;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

@Component("importarDevicesBean")
@ViewScoped
public class ImportarDevicesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private DeviceImportService deviceImportService;

    // lista de devices já importados, exibida na tabela no final da página
    private List<DeviceImportResponseDTO> devices;

    // arquivo vinculado ao card "Importar dispositivos" via mode="simple"
    private UploadedFile arquivoDispositivos;

    public void carregar() {
        devices = deviceImportService.listarTodos();
    }

    /**
     * Chamado pelo commandButton do card "Importar dispositivos".
     * Aceita JSON, CSV ou TXT com campos: nome, identificador, empresaId
     */
    public void importarDispositivos() {
        if (arquivoDispositivos == null) {
            addMensagemErro("Selecione um arquivo antes de importar");
            return;
        }
        System.out.println(">>> UPLOAD RECEBIDO: " + arquivoDispositivos.getFileName());
        try {
            DeviceImportService.ResultadoImportacao resultado =
                    deviceImportService.importarArquivo(
                            arquivoDispositivos.getInputStream(),
                            arquivoDispositivos.getFileName());
            carregar();

            if (resultado.getFalhas() == 0) {
                addMensagemSucesso(resultado.sucesso() + " device(s) importado(s) com sucesso");
            } else {
                addMensagemAviso(resultado.sucesso() + " importado(s), "
                        + resultado.getFalhas() + " falha(s): "
                        + String.join(" | ", resultado.erros()));
            }
        } catch (Exception e) {
            addMensagemErro("Erro ao processar arquivo: " + e.getMessage());
        }
    }

    /**
     * Handler genérico para os 8 cards ainda sem lógica implementada.
     * Mantido com FileUploadEvent caso algum card seja migrado para mode="advanced" depois.
     */
    public void uploadPendente(FileUploadEvent event) {
        addMensagemAviso("Arquivo \"" + event.getFile().getFileName()
                + "\" recebido. Esta funcionalidade ainda será implementada.");
    }

    private void addMensagemSucesso(String mensagem) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, mensagem, null));
    }

    private void addMensagemAviso(String mensagem) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, mensagem, null));
    }

    private void addMensagemErro(String mensagem) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, mensagem, null));
    }

    public List<DeviceImportResponseDTO> getDevices() { return devices; }

    public UploadedFile getArquivoDispositivos() { return arquivoDispositivos; }
    public void setArquivoDispositivos(UploadedFile arquivoDispositivos) {
        this.arquivoDispositivos = arquivoDispositivos;
    }
}