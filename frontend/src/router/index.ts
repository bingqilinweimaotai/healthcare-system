import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { guest: true } },
    { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { guest: true } },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { auth: true },
      children: [
        {
          path: '',
          redirect: () => {
            const r = useAuthStore().role
            if (r === 'ADMIN') return '/admin/dashboard'
            if (r === 'DOCTOR') return '/doctor/dashboard'
            if (r === 'PATIENT') return '/patient/dashboard'
            return '/login'
          },
        },
        // 患者端
        { path: 'patient/dashboard', name: 'PatientDashboard', component: () => import('@/views/patient/Dashboard.vue') },
        { path: 'patient/ai-consult', name: 'AiConsult', component: () => import('@/views/patient/AiConsult.vue') },
        { path: 'patient/manual-consult', name: 'ManualConsult', component: () => import('@/views/patient/ManualConsult.vue') },
        { path: 'patient/history', name: 'PatientHistory', component: () => import('@/views/patient/History.vue') },
        // 医生端
        { path: 'doctor/dashboard', name: 'DoctorDashboard', component: () => import('@/views/doctor/Dashboard.vue') },
        { path: 'doctor/sessions', name: 'DoctorSessions', component: () => import('@/views/doctor/Sessions.vue') },
        { path: 'doctor/prescriptions', name: 'DoctorPrescriptions', component: () => import('@/views/doctor/Prescriptions.vue') },
        // 管理端
        { path: 'admin/dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue') },
        { path: 'admin/users', name: 'AdminUsers', component: () => import('@/views/admin/Users.vue') },
        { path: 'admin/doctors', name: 'AdminDoctors', component: () => import('@/views/admin/Doctors.vue') },
        { path: 'admin/drugs', name: 'AdminDrugs', component: () => import('@/views/admin/Drugs.vue') },
      ],
    },
  ],
})

router.beforeEach(async (to, _from, next) => {
  try {
    const auth = useAuthStore()
    const hasToken = !!auth.tokenValue()
    if (hasToken && !auth.userId) await auth.fetchInfo()
    if (to.meta.auth && !auth.tokenValue()) return next('/login')
    if (to.meta.guest && hasToken && auth.role) return next('/')
    const role = auth.role
    if (to.path.startsWith('/admin') && role !== 'ADMIN') return next('/')
    if (to.path.startsWith('/doctor') && role !== 'DOCTOR') return next('/')
    if (to.path.startsWith('/patient') && role !== 'PATIENT') return next('/')
    next()
  } catch {
    next('/login')
  }
})

export default router
