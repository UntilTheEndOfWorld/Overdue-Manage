import {areaSelect} from "@/api/system/common";

const state = {
  areaSelect: []
}
const mutations = {
  SET_areaSelect: (state, value) => {
    state.areaSelect = value
  },
}
const actions = {
  loadAreaSelect({ commit, state }, force = false) {
    if (!force && state.areaSelect.length > 0) {
      return Promise.resolve();
    }
    return areaSelect({},{}).then(res => {
      commit('SET_areaSelect', res.data);
    })
  },
}
export default {
  namespaced: true,
  state,
  mutations,
  actions
}
