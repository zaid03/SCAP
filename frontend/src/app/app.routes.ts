import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { LoginComponent } from './login/login.component';
import { EntComponent } from './ent/ent.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { CentrogestorComponent } from './centrogestor/centrogestor.component';
import { EjeComponent } from './eje/eje.component';
import { ConsultaAnaliticaAlmacenesComponent } from './consulta-analitica-almacenes/consulta-analitica-almacenes.component';
import { ConsultaAnalticaDeArticulosComponent } from './consulta-analtica-de-articulos/consulta-analtica-de-articulos.component';
import { ConsultaAnalticaDeFamiliasComponent } from './consulta-analtica-de-familias/consulta-analtica-de-familias.component';
import { ConsultaArticulosAlmacenComponent } from './consulta-articulos-almacen/consulta-articulos-almacen.component';
import { ConsultaPendienteContabilizarComponent } from './consulta-pendiente-contabilizar/consulta-pendiente-contabilizar.component';
import { ConsultaGeneralArticulosComponent } from './consulta-general-articulos/consulta-general-articulos.component';
import { MantenimientoGeneralArticulosComponent } from './mantenimiento-general-articulos/mantenimiento-general-articulos.component';
import { ConsultaContabilizadoComponent } from './consulta-contabilizado/consulta-contabilizado.component';
import { FamiliaComponent } from './familia/familia.component';
import { TipoAlmacenajeComponent } from './tipo-almacenaje/tipo-almacenaje.component';
import { TiposUnidadesComponent } from './tipos-unidades/tipos-unidades.component';
import { MantinimientoArticulosAlmacenComponent } from './mantinimiento-articulos-almacen/mantinimiento-articulos-almacen.component';
import { ConsultaGeneralExistenciasComponent } from './consulta-general-existencias/consulta-general-existencias.component';
import { ConsultaExistenciasAlmacenComponent } from './consulta-existencias-almacen/consulta-existencias-almacen.component';
import { ConsultaSaldoCoontratosComponent } from './consulta-saldo-coontratos/consulta-saldo-coontratos.component';
import { ConsultaHistoricaAdContratosComponent } from './consulta-historica-ad-contratos/consulta-historica-ad-contratos.component';
import { ProveedoreesComponent } from './proveedorees/proveedorees.component';
import { FacturasComponent } from './facturas/facturas.component';
import { CreditoComponent } from './credito/credito.component';
import { CgeComponent } from './cge/cge.component';
import { ServiciosComponent } from './servicios/servicios.component';
import { EntregaComponent } from './entrega/entrega.component';
import { CosteComponent } from './coste/coste.component';
import { PersonaComponent } from './persona/persona.component';
import { EjercicioComponent } from './ejercicio/ejercicio.component';
import { PersonasPorServiciosComponent } from './personas-por-servicios/personas-por-servicios.component';
import { ConsultaProveedoresComponent } from './consulta-proveedores/consulta-proveedores.component';
import { ConsultaFacturaComponent } from './consulta-factura/consulta-factura.component';
import { BolsaCreditoComponent } from './bolsa-credito/bolsa-credito.component';
import { ConsultaBolsasComponent } from './consulta-bolsas/consulta-bolsas.component';
import { ContratosComponent } from './contratos/contratos.component';
import { MonitorContabilizacionComponent } from './monitor-contabilizacion/monitor-contabilizacion.component';

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'ent', component: EntComponent },
    { path: 'dashboard', component: DashboardComponent },
    { path: 'proveedorees', component: ProveedoreesComponent },
    { path: 'eje', component: EjeComponent},
    { path: 'centro-gestor', component: CentrogestorComponent},
    { path: 'almacenes', component: ConsultaAnaliticaAlmacenesComponent},
    { path: 'Carticulos', component: ConsultaAnalticaDeArticulosComponent},
    { path: 'Cfamilia', component: ConsultaAnalticaDeFamiliasComponent},
    { path: 'CAlmacen', component:ConsultaArticulosAlmacenComponent},
    { path: 'Ccontabilizar', component: ConsultaPendienteContabilizarComponent},
    { path: 'CGArticulos', component: ConsultaGeneralArticulosComponent},
    { path: 'MGArticulos', component: MantenimientoGeneralArticulosComponent},
    { path: 'MAlmacen', component: MantinimientoArticulosAlmacenComponent},
    { path: 'Ccontabilizado', component: ConsultaContabilizadoComponent},
    { path: 'familia', component: FamiliaComponent},
    { path: 'almacenaje', component: TipoAlmacenajeComponent},
    { path: 'unidades', component: TiposUnidadesComponent},
    { path: 'Cexistencias', component: ConsultaGeneralExistenciasComponent},
    { path: 'CExictenciasAlmacen', component: ConsultaExistenciasAlmacenComponent},
    { path: 'CSaldoCon', component: ConsultaSaldoCoontratosComponent},
    { path: 'CHistoriaADCon', component: ConsultaHistoricaAdContratosComponent},
    { path: 'facturas', component: FacturasComponent},
    { path: 'credito-Cge', component: CreditoComponent},
    { path: 'familia', component: FamiliaComponent},
    { path: 'centroGestor', component: CgeComponent},
    { path: 'servicios', component: ServiciosComponent},
    { path: 'entrega', component: EntregaComponent},
    { path: 'coste', component: CosteComponent},
    { path: 'persona', component: PersonaComponent},
    { path: 'ejercicios', component: EjercicioComponent},
    { path: 'personas-por-servicios', component: PersonasPorServiciosComponent },
    { path: 'Cproveedores', component: ConsultaProveedoresComponent},
    { path: 'Cfactura', component: ConsultaFacturaComponent},
    { path: 'credito', component: BolsaCreditoComponent},
    { path: 'Ccredito', component: ConsultaBolsasComponent},
    { path: 'contratos', component: ContratosComponent},
    { path: 'contabilizacion', component: MonitorContabilizacionComponent},
    { path: '', redirectTo: '/login', pathMatch: 'full' }, //route by default
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule],
})

export class AppRoutingModule {}