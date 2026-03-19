// 常數定義
export const REPORT_OPTIONS = [
  { value: 'SPAM', label: '廣告或商業推銷', color: 'orange' },
  { value: 'HARASSMENT', label: '仇恨或騷擾言論', color: 'red' },
  { value: 'IRRELEVANT', label: '與書籍內容無關', color: 'grey' },
  { value: 'INAPPROPRIATE', label: '不當內容', color: 'deep-orange' },
  { value: 'OTHER', label: '其他', color: 'blue-grey' }
]

// 輔助函式
export const getReportLabel = (value) => {
  const option = REPORT_OPTIONS.find(opt => opt.value === value)
  return option ? option.label : value
}

export const getReportColor = (value) => {
  const option = REPORT_OPTIONS.find(opt => opt.value === value)
  return option ? option.color : 'grey'
}