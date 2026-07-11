export const PROVINCE_LAYOUT = [
  { code: 'BJ', name: '北京市', shortName: '京', left: 73, top: 28 },
  { code: 'TJ', name: '天津市', shortName: '津', left: 77, top: 31 },
  { code: 'HE', name: '河北省', shortName: '冀', left: 72, top: 34 },
  { code: 'SX', name: '山西省', shortName: '晋', left: 65, top: 34 },
  { code: 'NM', name: '内蒙古自治区', shortName: '蒙', left: 53, top: 20 },
  { code: 'LN', name: '辽宁省', shortName: '辽', left: 82, top: 23 },
  { code: 'JL', name: '吉林省', shortName: '吉', left: 88, top: 20 },
  { code: 'HL', name: '黑龙江省', shortName: '黑', left: 93, top: 15 },
  { code: 'SH', name: '上海市', shortName: '沪', left: 86, top: 56 },
  { code: 'JS', name: '江苏省', shortName: '苏', left: 81, top: 52 },
  { code: 'ZJ', name: '浙江省', shortName: '浙', left: 84, top: 60 },
  { code: 'AH', name: '安徽省', shortName: '皖', left: 76, top: 55 },
  { code: 'FJ', name: '福建省', shortName: '闽', left: 81, top: 68 },
  { code: 'JX', name: '江西省', shortName: '赣', left: 74, top: 64 },
  { code: 'SD', name: '山东省', shortName: '鲁', left: 79, top: 42 },
  { code: 'HA', name: '河南省', shortName: '豫', left: 67, top: 50 },
  { code: 'HB', name: '湖北省', shortName: '鄂', left: 64, top: 59 },
  { code: 'HN', name: '湖南省', shortName: '湘', left: 61, top: 67 },
  { code: 'GD', name: '广东省', shortName: '粤', left: 66, top: 79 },
  { code: 'GX', name: '广西壮族自治区', shortName: '桂', left: 57, top: 78 },
  { code: 'HI', name: '海南省', shortName: '琼', left: 62, top: 93 },
  { code: 'CQ', name: '重庆市', shortName: '渝', left: 53, top: 62 },
  { code: 'SC', name: '四川省', shortName: '川', left: 46, top: 59 },
  { code: 'GZ', name: '贵州省', shortName: '贵', left: 50, top: 71 },
  { code: 'YN', name: '云南省', shortName: '云', left: 42, top: 77 },
  { code: 'XZ', name: '西藏自治区', shortName: '藏', left: 25, top: 62 },
  { code: 'SN', name: '陕西省', shortName: '陕', left: 57, top: 49 },
  { code: 'GS', name: '甘肃省', shortName: '甘', left: 46, top: 41 },
  { code: 'QH', name: '青海省', shortName: '青', left: 34, top: 46 },
  { code: 'NX', name: '宁夏回族自治区', shortName: '宁', left: 53, top: 40 },
  { code: 'XJ', name: '新疆维吾尔自治区', shortName: '新', left: 15, top: 33 },
  { code: 'TW', name: '台湾省', shortName: '台', left: 88, top: 74 },
  { code: 'HK', name: '香港特别行政区', shortName: '港', left: 70, top: 84 },
  { code: 'MO', name: '澳门特别行政区', shortName: '澳', left: 68, top: 85 }
]

export const PROVINCE_GB_CODE_MAP = {
  BJ: '11',
  TJ: '12',
  HE: '13',
  SX: '14',
  NM: '15',
  LN: '21',
  JL: '22',
  HL: '23',
  SH: '31',
  JS: '32',
  ZJ: '33',
  AH: '34',
  FJ: '35',
  JX: '36',
  SD: '37',
  HA: '41',
  HB: '42',
  HN: '43',
  GD: '44',
  GX: '45',
  HI: '46',
  CQ: '50',
  SC: '51',
  GZ: '52',
  YN: '53',
  XZ: '54',
  SN: '61',
  GS: '62',
  QH: '63',
  NX: '64',
  XJ: '65',
  TW: '71',
  HK: '81',
  MO: '82'
}

const MOCK_VISITS = [
  { code: 'YN', cityCount: 16, latestDate: '2026-02-14' },
  { code: 'SC', cityCount: 11, latestDate: '2025-11-03' },
  { code: 'GD', cityCount: 21, latestDate: '2026-01-21' },
  { code: 'ZJ', cityCount: 9, latestDate: '2025-12-30' },
  { code: 'FJ', cityCount: 6, latestDate: '2025-10-19' },
  { code: 'JS', cityCount: 7, latestDate: '2025-08-11' },
  { code: 'BJ', cityCount: 5, latestDate: '2025-06-02' },
  { code: 'SN', cityCount: 4, latestDate: '2025-04-28' },
  { code: 'HN', cityCount: 8, latestDate: '2026-02-02' }
]

export function isCityInProvince(cityCode, provinceCode) {
  const provinceGb = PROVINCE_GB_CODE_MAP[provinceCode]
  if (!provinceGb || !cityCode) return false
  return String(cityCode).startsWith(`CN-${provinceGb}-`)
}

export async function getMockProvinceMapData() {
  await new Promise((resolve) => setTimeout(resolve, 200))

  const visitMap = {}
  MOCK_VISITS.forEach((item) => {
    visitMap[item.code] = item
  })

  return PROVINCE_LAYOUT.map((p) => {
    const visit = visitMap[p.code]
    return {
      ...p,
      visited: !!visit,
      cityCount: (visit && visit.cityCount) || 0,
      latestDate: (visit && visit.latestDate) || ''
    }
  })
}
