<template>
  <div class="db">
    <!-- ════════ Hero Banner ════════ -->
    <div class="db-hero">
      <div class="db-hero__content">
        <div class="db-hero__left">
          <img :src="avatar" class="db-hero__avatar" alt="avatar" />
          <div>
            <h2 class="db-hero__greeting">{{ name }}，{{ hello }}</h2>
            <p class="db-hero__date"><i class="el-icon-date"></i> {{ nowTime }}</p>
          </div>
        </div>
        <div class="db-hero__right">
          <div class="db-hero__brand">
            <i class="el-icon-time db-hero__brand-icon"></i>
            <div>
              <div class="db-hero__brand-name">过期了吗</div>
              <div class="db-hero__brand-sub">Overdue · 后台管理系统</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ════════ KPI Row ════════ -->
    <div class="db-kpi">
      <div class="db-kpi__card" v-for="(c, i) in overviewCards" :key="i" @click="c.path && $router.push(c.path)">
        <div class="db-kpi__icon" :style="{ background: c.iconBg }">
          <i :class="c.icon"></i>
        </div>
        <div class="db-kpi__info">
          <div class="db-kpi__value">{{ c.value }}</div>
          <div class="db-kpi__label">{{ c.label }}</div>
        </div>
        <i class="el-icon-arrow-right db-kpi__arrow"></i>
      </div>
    </div>

    <!-- ════════ Quick Actions ════════ -->
    <div class="db-actions">
      <span class="db-actions__title"><i class="el-icon-s-operation"></i> 快捷操作</span>
      <div class="db-actions__list">
        <button class="db-chip" v-for="(a, i) in quickActions" :key="i" @click="$router.push(a.path)">
          <i :class="a.icon" :style="{ color: a.color }"></i>
          <span>{{ a.label }}</span>
        </button>
      </div>
    </div>

    <!-- ════════ Detail Panels ════════ -->
    <div class="db-panels">
      <!-- 物品过期统计 -->
      <div class="db-panel">
        <div class="db-panel__head">
          <div class="db-panel__dot" style="background: #1E40AF"></div>
          <h3>物品过期统计</h3>
        </div>
        <div class="db-panel__body">
          <div class="db-section">
            <div class="db-section__label">个人物品</div>
            <div class="db-bars">
              <div class="db-bar" v-for="(r, j) in personalExpiryList" :key="'p'+j">
                <div class="db-bar__top">
                  <span class="db-bar__name">{{ r.label }}</span>
                  <span class="db-bar__val" :style="{ color: r.color }">{{ r.value }}</span>
                </div>
                <div class="db-bar__track">
                  <div class="db-bar__fill" :style="{ width: barPct(r.value, personalItems.total), background: r.color }"></div>
                </div>
              </div>
            </div>
          </div>
          <hr class="db-hr" />
          <div class="db-section">
            <div class="db-section__label">共享物品</div>
            <div class="db-bars">
              <div class="db-bar" v-for="(r, j) in sharedExpiryList" :key="'s'+j">
                <div class="db-bar__top">
                  <span class="db-bar__name">{{ r.label }}</span>
                  <span class="db-bar__val" :style="{ color: r.color }">{{ r.value }}</span>
                </div>
                <div class="db-bar__track">
                  <div class="db-bar__fill" :style="{ width: barPct(r.value, sharedItems.total), background: r.color }"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 订单统计 -->
      <div class="db-panel">
        <div class="db-panel__head">
          <div class="db-panel__dot" style="background: #F59E0B"></div>
          <h3>订单统计</h3>
        </div>
        <div class="db-panel__body">
          <div class="db-section">
            <div class="db-section__label">支付状态</div>
            <div class="db-grid4">
              <div class="db-metric" v-for="(r, j) in orderStatusList" :key="'os'+j">
                <div class="db-metric__val">{{ r.value }}</div>
                <div class="db-metric__label"><span class="db-dot" :style="{ background: r.color }"></span>{{ r.label }}</div>
              </div>
            </div>
          </div>
          <hr class="db-hr" />
          <div class="db-section">
            <div class="db-section__label">套餐类型</div>
            <div class="db-grid4">
              <div class="db-metric" v-for="(r, j) in orderPlanList" :key="'op'+j">
                <div class="db-metric__val">{{ r.value }}</div>
                <div class="db-metric__label"><span class="db-dot" :style="{ background: r.color }"></span>{{ r.label }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 会员统计 -->
      <div class="db-panel">
        <div class="db-panel__head">
          <div class="db-panel__dot" style="background: #3B82F6"></div>
          <h3>会员统计</h3>
        </div>
        <div class="db-panel__body">
          <div class="db-section">
            <div class="db-section__label">会员状态</div>
            <div class="db-status-row">
              <div class="db-status-item" v-for="(r, j) in memberStatusList" :key="'ms'+j">
                <div class="db-status-ring" :style="{ borderColor: r.color }">
                  <span :style="{ color: r.color }">{{ r.value }}</span>
                </div>
                <div class="db-status-name">{{ r.label }}</div>
              </div>
            </div>
          </div>
          <hr class="db-hr" />
          <div class="db-section">
            <div class="db-section__label">套餐分布</div>
            <div class="db-grid4">
              <div class="db-metric" v-for="(r, j) in memberPlanList" :key="'mp'+j">
                <div class="db-metric__val">{{ r.value }}</div>
                <div class="db-metric__label"><span class="db-dot" :style="{ background: r.color }"></span>{{ r.label }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getOverview, getExpiryStats, getOrderStats, getMemberStats } from "@/api/overdue/statistics";

export default {
  name: "Dashboard",
  data() {
    return { nowTime: '', hello: '', overview: {}, expiryStats: {}, orderStats: {}, memberStats: {} }
  },
  computed: {
    ...mapGetters(['avatar', 'name']),
    overviewCards() {
      var o = this.overview;
      return [
        { label: '总用户数', value: o.totalUsers || 0, icon: 'el-icon-user-solid', iconBg: '#1E40AF', path: '/overdue/user' },
        { label: '个人物品', value: o.totalPersonalItems || 0, icon: 'el-icon-goods', iconBg: '#059669', path: '/overdue/personalItem' },
        { label: '共享空间', value: o.totalSharedSpaces || 0, icon: 'el-icon-office-building', iconBg: '#D97706', path: '/overdue/sharedSpace' },
        { label: '共享物品', value: o.totalSharedItems || 0, icon: 'el-icon-box', iconBg: '#7C3AED', path: '/overdue/sharedItem' },
        { label: '活跃会员', value: o.activeMembers || 0, icon: 'el-icon-star-on', iconBg: '#3B82F6', path: '/overdue/member' },
        { label: '总收入', value: o.totalRevenue ? '¥' + o.totalRevenue : '¥0', icon: 'el-icon-coin', iconBg: '#F59E0B', path: '/overdue/memberOrder' }
      ];
    },
    quickActions() {
      return [
        { label: '用户管理', icon: 'el-icon-user', color: '#1E40AF', path: '/overdue/user' },
        { label: '会员管理', icon: 'el-icon-star-on', color: '#3B82F6', path: '/overdue/member' },
        { label: '会员订单', icon: 'el-icon-tickets', color: '#F59E0B', path: '/overdue/memberOrder' },
        { label: '操作日志', icon: 'el-icon-document', color: '#059669', path: '/overdue/operationLog' },
        { label: '广告记录', icon: 'el-icon-video-play', color: '#DC2626', path: '/overdue/adWatchRecord' },
        { label: '空间成员', icon: 'el-icon-s-custom', color: '#7C3AED', path: '/overdue/spaceMember' }
      ];
    },
    personalItems() { return this.expiryStats.personalItems || { total: 0, expired: 0, expiringSoon: 0, normal: 0 }; },
    sharedItems() { return this.expiryStats.sharedItems || { total: 0, expired: 0, expiringSoon: 0, normal: 0 }; },
    personalExpiryList() {
      var p = this.personalItems;
      return [
        { label: '正常', value: p.normal || 0, color: '#059669' },
        { label: '即将过期', value: p.expiringSoon || 0, color: '#D97706' },
        { label: '已过期', value: p.expired || 0, color: '#DC2626' }
      ];
    },
    sharedExpiryList() {
      var s = this.sharedItems;
      return [
        { label: '正常', value: s.normal || 0, color: '#059669' },
        { label: '即将过期', value: s.expiringSoon || 0, color: '#D97706' },
        { label: '已过期', value: s.expired || 0, color: '#DC2626' }
      ];
    },
    orderStatusList() {
      var o = this.orderStats.byStatus || {};
      return [
        { label: '未支付', value: o.pending || 0, color: '#94A3B8' },
        { label: '已支付', value: o.paid || 0, color: '#059669' },
        { label: '支付失败', value: o.failed || 0, color: '#DC2626' },
        { label: '已退款', value: o.refunded || 0, color: '#D97706' }
      ];
    },
    orderPlanList() {
      var o = this.orderStats.byPlanType || {};
      return [
        { label: '月度', value: o.monthly || 0, color: '#3B82F6' },
        { label: '季度', value: o.quarterly || 0, color: '#059669' },
        { label: '年度', value: o.yearly || 0, color: '#F59E0B' },
        { label: '终身', value: o.lifetime || 0, color: '#DC2626' }
      ];
    },
    memberStatusList() {
      var m = this.memberStats.byStatus || {};
      return [
        { label: '活跃', value: m.active || 0, color: '#059669' },
        { label: '已过期', value: m.expired || 0, color: '#DC2626' }
      ];
    },
    memberPlanList() {
      var m = this.memberStats.byPlanType || {};
      return [
        { label: '月度', value: m.monthly || 0, color: '#3B82F6' },
        { label: '季度', value: m.quarterly || 0, color: '#059669' },
        { label: '年度', value: m.yearly || 0, color: '#F59E0B' },
        { label: '终身', value: m.lifetime || 0, color: '#DC2626' }
      ];
    }
  },
  created() {
    this.initTime();
    this.loadData();
  },
  methods: {
    initTime() {
      var d = new Date();
      var y = d.getFullYear();
      var m = String(d.getMonth() + 1).padStart(2, '0');
      var dd = String(d.getDate()).padStart(2, '0');
      var w = ['日', '一', '二', '三', '四', '五', '六'];
      this.nowTime = y + '年' + m + '月' + dd + '日 星期' + w[d.getDay()];
      var h = d.getHours();
      this.hello = h < 12 ? '上午好' : h < 18 ? '下午好' : '晚上好';
    },
    loadData() {
      var self = this;
      getOverview().then(function(r) { self.overview = (r && r.data) ? r.data : {}; }).catch(function() {});
      getExpiryStats().then(function(r) { self.expiryStats = (r && r.data) ? r.data : {}; }).catch(function() {});
      getOrderStats().then(function(r) { self.orderStats = (r && r.data) ? r.data : {}; }).catch(function() {});
      getMemberStats().then(function(r) { self.memberStats = (r && r.data) ? r.data : {}; }).catch(function() {});
    },
    barPct(val, total) {
      if (!total || !val) return '0%';
      return Math.min(Math.round(val / total * 100), 100) + '%';
    }
  }
}
</script>

<style scoped>
/* ── Design tokens (scoped) ───────────────────────────── */
.db {
  --primary: #1E40AF;
  --secondary: #3B82F6;
  --accent: #F59E0B;
  --bg: #F8FAFC;
  --text: #1E3A8A;
  --text-2: #64748B;
  --border: #E2E8F0;
  --card: #FFFFFF;
  --radius: 12px;
  --radius-sm: 8px;
  --shadow: 0 1px 3px rgba(0,0,0,.04), 0 4px 6px rgba(0,0,0,.04);
  --shadow-hover: 0 4px 12px rgba(0,0,0,.08);
  --ease: 200ms ease;

  padding: 0 0 32px;
  background: var(--bg);
  min-height: calc(100vh - 84px);
}

/* ── Hero ───────────────────────────────────────────── */
.db-hero {
  margin: 20px;
  border-radius: var(--radius);
  background: var(--primary);
  color: #fff;
  overflow: hidden;
  position: relative;
}

.db-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 15% 85%, rgba(59,130,246,.4), transparent 50%),
    radial-gradient(ellipse at 85% 15%, rgba(245,158,11,.15), transparent 50%);
  pointer-events: none;
}

.db-hero__content {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 26px 32px;
}

.db-hero__left { display: flex; align-items: center; gap: 16px; }

.db-hero__avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: 2px solid rgba(255,255,255,.3);
  object-fit: cover;
}

.db-hero__greeting {
  margin: 0 0 2px;
  font-size: 18px;
  font-weight: 700;
}

.db-hero__date {
  margin: 0;
  font-size: 13px;
  opacity: .75;
}

.db-hero__date i { margin-right: 4px; }

.db-hero__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(255,255,255,.1);
  padding: 8px 18px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255,255,255,.12);
}

.db-hero__brand-icon { font-size: 24px; opacity: .9; }
.db-hero__brand-name { font-size: 15px; font-weight: 700; letter-spacing: 1.5px; }
.db-hero__brand-sub { font-size: 11px; opacity: .6; margin-top: 1px; }

/* ── KPI Cards ──────────────────────────────────────── */
.db-kpi {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
  padding: 0 20px;
  margin-top: -14px;
  position: relative;
  z-index: 2;
}

.db-kpi__card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--card);
  border-radius: var(--radius);
  padding: 16px 14px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
  cursor: pointer;
  transition: transform var(--ease), box-shadow var(--ease);
  position: relative;
}

.db-kpi__card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-hover);
}

.db-kpi__icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
}

.db-kpi__icon i { font-size: 20px; }

.db-kpi__value {
  font-size: 22px;
  font-weight: 800;
  color: var(--text);
  line-height: 1.15;
}

.db-kpi__label {
  font-size: 12px;
  color: var(--text-2);
  margin-top: 2px;
  font-weight: 500;
}

.db-kpi__arrow {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0;
  color: var(--text-2);
  font-size: 14px;
  transition: opacity var(--ease);
}

.db-kpi__card:hover .db-kpi__arrow { opacity: .5; }

/* ── Quick Actions ──────────────────────────────────── */
.db-actions {
  margin: 20px 20px 0;
  padding: 14px 20px;
  background: var(--card);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  gap: 16px;
}

.db-actions__title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
  white-space: nowrap;
}

.db-actions__title i { margin-right: 4px; color: var(--primary); }

.db-actions__list { display: flex; flex-wrap: wrap; gap: 8px; }

.db-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  border-radius: 20px;
  background: var(--bg);
  font-size: 12px;
  color: #334155;
  cursor: pointer;
  border: 1px solid transparent;
  font-weight: 500;
  transition: all var(--ease);
  outline: none;
  font-family: inherit;
}

.db-chip:hover {
  background: var(--card);
  border-color: var(--secondary);
  color: var(--primary);
  box-shadow: 0 2px 6px rgba(30,64,175,.1);
}

.db-chip:focus-visible {
  outline: 2px solid var(--secondary);
  outline-offset: 2px;
}

.db-chip i { font-size: 14px; }

/* ── Detail Panels ──────────────────────────────────── */
.db-panels {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 20px 20px 0;
}

.db-panel {
  background: var(--card);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
  transition: transform var(--ease), box-shadow var(--ease);
  overflow: hidden;
}

.db-panel:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.db-panel__head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  border-bottom: 1px solid #F1F5F9;
}

.db-panel__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.db-panel__head h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--text);
}

.db-panel__body { padding: 16px 20px 20px; }

.db-hr {
  border: none;
  height: 1px;
  background: #F1F5F9;
  margin: 14px 0;
}

/* ── Section labels ─────────────────────────────────── */
.db-section__label {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-2);
  text-transform: uppercase;
  letter-spacing: .8px;
  margin-bottom: 10px;
}

/* ── Progress bars ──────────────────────────────────── */
.db-bars { display: flex; flex-direction: column; gap: 8px; }

.db-bar__top {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 3px;
}

.db-bar__name { font-size: 12px; color: #475569; font-weight: 500; }
.db-bar__val { font-size: 14px; font-weight: 800; }

.db-bar__track {
  height: 6px;
  border-radius: 3px;
  background: #F1F5F9;
  overflow: hidden;
}

.db-bar__fill {
  height: 100%;
  border-radius: 3px;
  transition: width .5s ease;
}

/* ── Metric grid (2x2) ─────────────────────────────── */
.db-grid4 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.db-metric {
  background: var(--bg);
  border-radius: var(--radius-sm);
  padding: 12px 10px;
  text-align: center;
  border: 1px solid #F1F5F9;
  transition: background var(--ease), border-color var(--ease);
  cursor: default;
}

.db-metric:hover {
  background: var(--card);
  border-color: var(--border);
}

.db-metric__val {
  font-size: 20px;
  font-weight: 800;
  color: var(--text);
  line-height: 1.2;
}

.db-metric__label {
  font-size: 11px;
  color: var(--text-2);
  margin-top: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-weight: 500;
}

.db-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

/* ── Status rings ───────────────────────────────────── */
.db-status-row {
  display: flex;
  justify-content: center;
  gap: 32px;
  padding: 6px 0;
}

.db-status-item { text-align: center; }

.db-status-ring {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 3px solid;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 6px;
  background: var(--card);
  transition: transform var(--ease);
  cursor: default;
}

.db-status-ring:hover { transform: scale(1.06); }
.db-status-ring span { font-size: 18px; font-weight: 800; }
.db-status-name { font-size: 11px; color: var(--text-2); font-weight: 600; }

/* ── Responsive: reduced motion ─────────────────────── */
@media (prefers-reduced-motion: reduce) {
  .db-kpi__card,
  .db-panel,
  .db-metric,
  .db-status-ring,
  .db-chip,
  .db-bar__fill {
    transition: none !important;
  }
}
</style>
