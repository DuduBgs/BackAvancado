package br.edu.uniesp.assistencia.internal.ordemservico.service;

import br.edu.uniesp.assistencia.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.assistencia.internal.cliente.repository.ClienteRepository;
import br.edu.uniesp.assistencia.internal.equipamento.entity.EquipamentoEntity;
import br.edu.uniesp.assistencia.internal.equipamento.repository.EquipamentoRepository;
import br.edu.uniesp.assistencia.internal.ordemservico.dto.CriarOrdemServicoRequest;
import br.edu.uniesp.assistencia.internal.ordemservico.dto.CriarOrdemServicoResponse;
import br.edu.uniesp.assistencia.internal.ordemservico.entity.OrdemServicoEntity;
import br.edu.uniesp.assistencia.internal.ordemservico.repository.OrdemServicoRepository;
import br.edu.uniesp.assistencia.shared.exception.RecursoNaoEncontradoException;
import br.edu.uniesp.assistencia.shared.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteRepository clienteRepository;
    private final EquipamentoRepository equipamentoRepository;

    @Transactional
    public CriarOrdemServicoResponse abrirOrdem(CriarOrdemServicoRequest request) {
        ClienteEntity cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", request.clienteId()));
        EquipamentoEntity equipamento = equipamentoRepository.findById(request.equipamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Equipamento", request.equipamentoId()));

        if (!equipamento.getCliente().getId().equals(cliente.getId())) {
            throw new RegraNegocioException("O equipamento informado não pertence ao cliente informado.");
        }

        String numeroProtocolo = gerarNumeroProtocolo();
        OrdemServicoEntity ordem = ordemServicoRepository.save(
                new OrdemServicoEntity(numeroProtocolo, cliente, equipamento, request.descricaoProblema()));

        return new CriarOrdemServicoResponse(
                ordem.getId(),
                ordem.getNumeroProtocolo(),
                ordem.getStatus(),
                ordem.getDataAbertura());
    }

    private String gerarNumeroProtocolo() {
        String dataAtual = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String numeroProtocolo;
        do {
            int sufixo = ThreadLocalRandom.current().nextInt(0x10000);
            numeroProtocolo = String.format(Locale.ROOT, "OS-%s-%04X", dataAtual, sufixo);
        } while (ordemServicoRepository.existsByNumeroProtocolo(numeroProtocolo));
        return numeroProtocolo;
    }
}
