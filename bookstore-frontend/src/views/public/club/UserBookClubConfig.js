export const headers = [
  { title: '讀書會名稱', key: 'clubName', sortable: true },
  { title: '活動時間', key: 'eventDate', sortable: true },
  { title: '狀態', key: 'status', sortable: true },
  { title: '人數', key: 'participants', sortable: true },
  { title: '地點', key: 'location', sortable: false },
  { title: '詳細資訊', key: 'details', sortable: false },
  { title: '報名', key: 'actions', sortable: false },
]

export const statusMap = {
  0: { text: '審核中', color: 'orange' },
  1: { text: '報名中', color: 'success' },
  2: { text: '已駁回', color: 'error' },
  3: { text: '已額滿', color: 'purple' },
  4: { text: '已截止', color: 'grey' },
  5: { text: '已結束', color: 'brown' },
  6: { text: '已取消', color: 'grey-darken-1' },
  7: { text: '草稿', color: 'grey-lighten-1' },
}

export const statusOptions = [
  { title: '全部', value: null },
  { title: '審核中', value: 0 },
  { title: '報名中', value: 1 },
  { title: '已駁回', value: 2 },
  { title: '已額滿', value: 3 },
  { title: '已截止', value: 4 },
  { title: '已結束', value: 5 },
  { title: '已取消', value: 6 },
  { title: '草稿中', value: 7 },
]
