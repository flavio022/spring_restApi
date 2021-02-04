package com.cursomc.services;

import java.util.Date;
import java.util.Optional;

import com.cursomc.domain.ItemPedido;
import com.cursomc.domain.PagamentoComBoleto;
import com.cursomc.domain.enums.EstadoPagamento;
import com.cursomc.respositories.ItemPedidoRepository;
import com.cursomc.respositories.PagamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.cursomc.domain.Pedido;
import com.cursomc.respositories.PedidoRepository;
import com.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {
	@Autowired
	private PedidoRepository repo;
	@Autowired
	private PedidoService pedidoService;
	@Autowired
	private ClienteService clienteService;
	@Autowired
	private PagamentoRepository pagamentoRepository;
	@Autowired
	private BoletoService boletoService;
	@Autowired
	private ProdutoService produtoService;
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;

	@Autowired
	private EmailService emailService;

	public Pedido buscar(Integer id) {
		Optional<Pedido> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Pedido.class.getName()));
	}



	public Pedido insert(Pedido obj) {
		obj.setId(null);
		obj.setInstante(new Date());
		obj.setCliente(clienteService.find(obj.getCliente().getId()));
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		if (obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagto, obj.getInstante());
		}
		obj = repo.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		for (ItemPedido ip : obj.getItens()) {
			ip.setDesconto(0.0);
			ip.setProduto(produtoService.buscar(ip.getProduto().getId()));
			ip.setPreco(ip.getProduto().getPreco());
			ip.setPedido(obj);
		}
		itemPedidoRepository.saveAll(obj.getItens());
		//emailService.sendOrderConfirmationEmail(obj);
		emailService.sendOrderConfirmationHtmlEmail(obj);

		return obj;
	}
}
