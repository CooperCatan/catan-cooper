import React from 'react';

// Define interfaces for the structure of cost items
interface CostItem {
  name: string;
  vp: string;
  resources: { name: string; count: number; icon?: string }[]; // icon can be path to small resource image or emoji
  note?: string;
}

const buildingCostsData: CostItem[] = [
  {
    name: 'Road',
    vp: '0 Victory Points',
    resources: [
      { name: 'Wood', count: 1, icon: '🌲' }, // Replace with /assets/images/resource-cards/wood-card.png if desired
      { name: 'Brick', count: 1, icon: '🧱' }, // Replace with /assets/images/resource-cards/brick-card.png
    ],
  },
  {
    name: 'Settlement',
    vp: '1 VP',
    resources: [
      { name: 'Wood', count: 1, icon: '🌲' },
      { name: 'Brick', count: 1, icon: '🧱' },
      { name: 'Wheat', count: 1, icon: '🌾' },
      { name: 'Sheep', count: 1, icon: '🐑' },
    ],
  },
  {
    name: 'City',
    vp: '2 VPs',
    resources: [
      { name: 'Wheat', count: 2, icon: '🌾' },
      { name: 'Ore', count: 3, icon: '⛰️' }, // Replace with /assets/images/resource-cards/ore-card.png
    ],
    note: 'A City replaces an already-built Settlement.',
  },
  {
    name: 'Development Card',
    vp: '? VPs',
    resources: [
      { name: 'Sheep', count: 1, icon: '🐑' },
      { name: 'Wheat', count: 1, icon: '🌾' },
      { name: 'Ore', count: 1, icon: '⛰️' },
    ],
    note: 'Usually, you only play 1 Development Card per turn, and you cannot play a Development Card on the turn it\'s built.',
  },
];

const BuildingCosts: React.FC = () => {
  return (
    <div className="p-3 bg-yellow-50/80 backdrop-blur-sm border border-yellow-300/50 rounded-lg shadow-lg max-w-sm w-full text-sm">
      <h3 className="text-center text-lg font-bold text-yellow-800/90 mb-3 tracking-wide">BUILDING COSTS</h3>
      <div className="space-y-3">
        {buildingCostsData.map((item) => (
          <div key={item.name} className="border-b border-yellow-400/30 pb-2 last:border-b-0 last:pb-0">
            <div className="flex justify-between items-center mb-1">
              <span className="font-semibold text-yellow-700/90 text-base">{item.name}</span>
              <span className="text-xs text-yellow-600/80">{item.vp}</span>
            </div>
            <div className="flex items-center space-x-1.5 mb-1">
              {item.resources.map((res, index) => (
                <div key={index} className="flex items-center bg-yellow-100/70 rounded px-1.5 py-0.5 text-xs shadow-sm">
                  {/* For actual images: <img src={res.icon} alt={res.name} className="w-4 h-auto mr-1" /> */}
                  <span className="mr-0.5 text-lg">{res.icon}</span>
                  <span>{res.count}</span>
                </div>
              ))}
            </div>
            {item.note && <p className="text-xs text-yellow-600/80 mt-1">• {item.note}</p>}
          </div>
        ))}
      </div>
    </div>
  );
};

export default BuildingCosts; 